/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.Event;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class TcpConnection extends EventHandlerBase implements Connection {

	private static final int IO_READ_LIMIT_PER_ROUND = 64 * 1024;

	private static final int IO_WRITE_LIMIT_PER_ROUND = 64 * 1024;

	private final AtomicLong rx = new AtomicLong(0);

	private final AtomicLong tx = new AtomicLong(0);

	private final SocketChannel channel;

	private volatile boolean receiveEnabled = true;

	private volatile boolean notifyWhenWritable = false;

	private volatile boolean registerForWrite = false;

	// private boolean lastWriteBlocked = false;

	private Session session;

	private final SocketAddress remoteAddress;

	private TcpConnection(NetContext context, SocketChannel channel, SocketAddress remoteAddress) {
		super(context);
		this.channel = channel;
		this.remoteAddress = remoteAddress;
	}

	public static TcpConnection create(NetContext context, SocketChannel channel, SocketAddress remoteAddress) throws IOException {
		Validation.notNull(context);
		Validation.notNull(channel);
		// make the socket ready for the session to write to
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());
		TcpConnection conn = new TcpConnection(context, channel, remoteAddress);
		conn.setup();
		return conn;
	}

	private void setup() throws IOException {
		try {
			session = context.getSessionFactory().createSession(this);
		} catch (Exception e) {
			throw new IOException("session-creation failed", e);
		}
		context.getEventDispatcher().register(this, Event.READ);
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public int handle(int events) throws IOException {
		// only the read and/or write flags may be set
		assert ((events & Event.READ_WRITE) != 0 && (events & ~Event.READ_WRITE) == 0);

		if (Event.hasRead(events) && receiveEnabled) {
			handleRead();
		}

		if (Event.hasWrite(events) && notifyWhenWritable) {
			notifyWhenWritable = false;
			session.writable(this);
		}

		int newEvents = 0;
		if (receiveEnabled) {
			newEvents |= Event.READ;
		}
		if (registerForWrite) {
			newEvents |= Event.WRITE;
		}
		return newEvents;
	}

	@Override
	public void close() {
		context.getEventDispatcher().unregister(this);
		if (channel.isOpen()) {
			SafeCloseUtil.close(channel);
			if (session != null) {
				session.closed(this);
			}
		}
	}

	@Override
	public int send(final ByteBuffer data) throws IOException {
		if (data == null || !data.hasRemaining()) {
			return 0;
		}
		int numWritten = channelWrite(data);
		if (data.hasRemaining()) {
			registerForWriteEvents();
		}
		else {
			unregisterFromWriteEvents();
		}
		return numWritten;
	}

	private int channelWrite(final ByteBuffer data) throws IOException {
		int num = channel.write(data);
		if (num > 0) {
			tx.addAndGet(num);
		}
		return num;
	}

	private int channelRead(final ByteBuffer rbuf) throws IOException {
		int num = channel.read(rbuf);
		if (num > 0) {
			rx.addAndGet(num);
		}
		return num;
	}

	private void handleRead() throws IOException {
		final ByteBuffer bb = takeBuffer();
		int totalRead = 0;
		while (totalRead < IO_READ_LIMIT_PER_ROUND) {
			int numChanRead = 0;
			if (bb.hasRemaining()) {
				// lets try to fill this buffer
				numChanRead = channelRead(bb);
			}
			// deliver data regardless of whether the channel is closed or not
			deliverReadData(bb);
			if (numChanRead == -1) {
				if (bb.position() > 0) {
					System.err.println("connection was closed and the corresponding session did not consume all read data");
				}
				// connection closed
				r_partialConsume = null;
				freeBuffer(bb);
				close();
				return;
			}
			if (numChanRead == 0) {
				break;
			}
			if (bb.hasRemaining()) {
				totalRead += numChanRead;
			}
			else {
				// if the read buffer is full we cant continue reading until
				// the client has consumed its pending data.
				break;
			}
		}
		if (bb.position() > 0) {
			// the client did not consume all the data, therefore we do not
			// release this buffer but store it so that we can try to have
			// the client consume it later on
			if (r_partialConsume == null) {
				// not registered for partial consume events
				registerForDeliverPartialReads();
			}
			r_partialConsume = bb;
		}
		else {
			if (r_partialConsume != null) {
				// registered for partial consume events
				unregisterFromDeliverPartialReads();
			}
			r_partialConsume = null;
			freeBuffer(bb);
		}
		Validation.isTrue((r_partialConsume == null) || (r_partialConsume.position() > 0));
	}

	private void deliverReadData(final ByteBuffer data) throws IOException {
		if (data.position() > 0) {
			// switch from write mode to read
			data.flip();
			session.receive(this, data);
			// switch back to write mode
			data.clear();
		}
	}

	private void registerForWriteEvents() {
		if (!registeredForWriteEvt) {
			context.getEventDispatcher().changeOps(this, Event.READ_WRITE, true);
			registeredForWriteEvt = true;
		}
	}

	private void unregisterFromWriteEvents() {
		if (registeredForWriteEvt) {
			context.getEventDispatcher().changeOps(this, Event.READ, false);
			registeredForWriteEvt = false;
		}
	}

	// private void registerForDeliverPartialReads() {
	// context.getEventDispatcher().registerForRedeliverPartialReads(this);
	// }
	//
	// private void unregisterFromDeliverPartialReads() {
	// context.getEventDispatcher().unregisterFromRedeliverPartialReads(this);
	// }

	@Override
	public long getRx() {
		return rx.get();
	}

	@Override
	public long getTx() {
		return tx.get();
	}

	boolean isRegisteredForWriteEvents() {
		return registeredForWriteEvt;
	}

	@Override
	public void notifyWhenNextWritable() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableReceive() {
		receiveEnabled = false;
	}

	@Override
	public void enableReceive() {
		receiveEnabled = true;
		boolean asap = !context.getEventDispatcher().isEventDispatcherThread();
		context.getEventDispatcher().changeOps(this, calcOps(), asap);
	}

	@Override
	public boolean isReceiveEnabled() {
		return receiveEnabled;
	}
}
