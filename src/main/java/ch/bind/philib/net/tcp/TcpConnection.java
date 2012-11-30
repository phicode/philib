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
import ch.bind.philib.net.InterestedEvents;
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

	private final SocketAddress remoteAddress;

	private volatile InterestedEvents interestedEvents = InterestedEvents.SENDABLE_RECEIVE;

	// private volatile boolean receiveEnabled = true;

	// private volatile boolean notifyWhenWritable = false;

	// private volatile boolean registerForWrite = false;

	// private boolean lastWriteBlocked = false;

	private Session session;

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
	public void setEvents(InterestedEvents interestedEvents) {
		this.interestedEvents = interestedEvents;
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

		if (Event.hasRead(events) && interestedEvents.hasReceive()) {
			handleRead();
		}

		if (Event.hasWrite(events) && interestedEvents.hasSendable()) {
			interestedEvents = session.sendable(this);
		}

		InterestedEvents ie = interestedEvents;
		return ie == null ? Event.DONT_CHANGE : ie.getEventMask();
	}

	@Override
	public void close() {
		context.getEventDispatcher().unregister(this);
		if (channel.isOpen()) {
			SafeCloseUtil.close(channel);
		}
		if (session != null) {
			session.closed(this);
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
		} else {
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
		while (totalRead < IO_READ_LIMIT_PER_ROUND && interestedEvents.hasReceive()) {
			final int n = channelRead(bb);
			// deliver data regardless of whether the channel is closed or not
			if (n == -1) {
				// connection closed
				close();
				return;
			} else {
				if (n == 0) {
					break;
				}
				totalRead += n;
				deliverReadData(bb);
			}
		}
	}

	private void deliverReadData(final ByteBuffer data) throws IOException {
		if (data.position() > 0) {
			// switch from write mode to read
			data.flip();
			interestedEvents = session.receive(this, data);
			// switch back to write mode
			data.clear();
		}
	}

	@Override
	public long getRx() {
		return rx.get();
	}

	@Override
	public long getTx() {
		return tx.get();
	}
}
