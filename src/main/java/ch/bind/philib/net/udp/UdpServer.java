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

package ch.bind.philib.net.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.net.DatagramSession;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.net.events.NetBuf;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class UdpServer extends EventHandlerBase implements NetServer {

	private static final int IO_READ_LIMIT_PER_ROUND = 64 * 1024;

	private static final int IO_WRITE_LIMIT_PER_ROUND = 64 * 1024;

	private final DatagramChannel channel;

	private final DatagramSession session;

	UdpServer(NetContext context, DatagramSession session, DatagramChannel channel) {
		super(context);
		Validation.notNull(session);
		Validation.notNull(channel);
		this.session = session;
		this.channel = channel;
	}

	void setup() throws IOException {
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());
		context.getEventDispatcher().register(this, EventUtil.READ);
	}

	static UdpServer open(NetContext context, DatagramSession session, SocketAddress bindAddress) throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		DatagramSocket socket = channel.socket();
		socket.bind(bindAddress);
		// TODO: log bridge
		System.out.println("UDP listening on: " + bindAddress);

		UdpServer server = new UdpServer(context, session, channel);
		server.setup();
		return server;
	}

	@Override
	public void close() throws IOException { // TODO
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getActiveSessionCount() { // TODO
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	// @Override
	// TODO
	// public Connection connect(SocketAddress addr) {
	// channel.con
	// }

	private void notifyReceive(SocketAddress addr, ByteBuffer rbuf) {
		try {
			session.receive(addr, rbuf);
		} catch (IOException e) {
			// TODO Auto-generated method stub
			e.printStackTrace(System.err);
		}
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public int handle(int ops) throws IOException {
		// TODO
		final ByteBuffer rbuf = acquireBuffer();
		try {
			int totalRead = 0;
			while (totalRead < IO_READ_LIMIT_PER_ROUND) {
				rbuf.clear();
				SocketAddress addr = channel.receive(rbuf);
				if (addr == null) {
					// no more data to read
					assert (rbuf.position() == 0 && rbuf.remaining() == 0);
					return EventUtil.READ;
				}
				rbuf.flip();
				// assert (num == rbuf.limit());
				// assert (num == rbuf.remaining());
				totalRead += rbuf.remaining();
				notifyReceive(addr, rbuf);
			}
		} finally {
			releaseBuffer(rbuf);
		}
		return EventUtil.READ;
	}

	void sendSync(final SocketAddress addr, final ByteBuffer data) throws IOException {
		Validation.notNull(addr);
		if (data == null || !data.hasRemaining()) {
			return;
		}
		if (getContext().getEventDispatcher().isEventDispatcherThread(Thread.currentThread())) {
			throw new IllegalStateException("cant write in blocking mode from the dispatcher thread");
		}

		// first the remaining data in the backlog has to be written (if
		// any), then our buffer
		// if in the meantime more data arrives we do not want to block
		// longer
		final NetBuf externBuf = NetBuf.createExtern(data);
		synchronized (w_writeBacklog) {
			w_writeBacklog.addBack(externBuf);
			while (true) {
				boolean finished = sendPendingAsync();

				if (finished) {
					// all data from the backlog has been written
					assert (!externBuf.isPending() && !data.hasRemaining());
					unregisterFromWriteEvents();
					return;
				}
				registerForWriteEvents();

				// not all data in the backlog has been written
				if (externBuf.isPending()) {
					// our data is among those who are waiting to be written
					w_writeBacklog.wait();
				} else {
					// our data has been written
					assert (!data.hasRemaining());
					return;
				}
			}
		}
	}

	void sendAsync(final SocketAddress addr, final ByteBuffer data) throws IOException {
		Validation.notNull(addr);
		if (data == null || !data.hasRemaining()) {
			return;
		}

	}
}
