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

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.net.Events;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.conn.ConnectionBase;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.SelectOps;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class TcpConnection extends ConnectionBase {

	// TODO: configurable
	private static final int IO_READ_LIMIT_PER_ROUND = 256 * 1024;

	protected final SocketChannel channel;

	protected final SocketAddress remoteAddress;

	protected volatile Events events = Events.SENDABLE_RECEIVE;

	private Session session;

	TcpConnection(NetContext context, SocketChannel channel, SocketAddress remoteAddress) {
		super(context);
		Validation.notNull(channel);
		this.channel = channel;
		this.remoteAddress = remoteAddress;
	}

	// for already connected channels
	static TcpConnection createConnected(NetContext context, SocketChannel channel, SocketAddress remoteAddress) throws IOException {
		TcpConnection conn = new TcpConnection(context, channel, remoteAddress);
		conn.setupChannel();
		conn.setupSession();
		context.getEventDispatcher().register(conn, conn.events.getEventMask());
		return conn;
	}

	final void setupChannel() throws IOException {
		// make the socket ready for the session to write to
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());
	}

	final void setupSession() throws IOException {
		try {
			session = context.getSessionManager().createSession(this);
		} catch (Exception e) {
			throw new IOException("session-creation failed", e);
		}
		if (session == null) {
			throw new IOException("session-creation failed: no session provided");
		}
	}

	@Override
	public void setEvents(Events events) {
		Validation.notNull(events);
		this.events = events;
		context.getEventDispatcher().register(this, events.getEventMask());
	}

	@Override
	public int handleOps(final int ops) throws IOException {
		// only the read and/or write flags may be set
		assert ((ops & SelectOps.READ_WRITE) != 0 && (ops & ~SelectOps.READ_WRITE) == 0);

		if (SelectOps.hasRead(ops) && events.hasReceive()) {
			// updates "events", so dont cache this volatile field
			handleRead();
		}

		if (SelectOps.hasWrite(ops) && events.hasSendable()) {
			events = session.sendable(this);
		}

		return events.getEventMask();
	}

	@Override
	public boolean handleTimeout() throws IOException {
		// TODO: implement read & write timeouts
		return true;
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
		int num = channel.write(data);
		if (num > 0) {
			incrementTx(num);
		}
		return num;
	}

	private void handleRead() throws IOException {
		final ByteBuffer bb = takeBuffer();
		try {
			int totalRead = 0;
			while (totalRead < IO_READ_LIMIT_PER_ROUND && events.hasReceive()) {
				final int n = channel.read(bb);
				if (n == 0) {
					break;
				}
				if (n == -1) {
					// connection closed
					close();
					return;
				}
				incrementRx(n);
				totalRead += n;
				// switch from write mode to read
				bb.flip();
				events = session.receive(this, bb);
				// switch back to write mode
				bb.clear();
			}
		} finally {
			recycleBuffer(bb);
		}
	}

	@Override
	public final boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public final boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public final SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public final SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public final Session getSession() {
		return session;
	}
}
