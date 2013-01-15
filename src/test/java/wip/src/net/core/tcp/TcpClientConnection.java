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

package wip.src.net.core.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

import wip.src.net.core.Events;
import wip.src.net.core.conn.ConnectTimeoutException;
import wip.src.net.core.context.NetContext;
import wip.src.net.core.events.SelectOps;

import ch.bind.philib.validation.Validation;

public class TcpClientConnection extends TcpConnection {

	// not null while connecting
	private volatile AsyncConnectFuture<TcpConnection> future;

	public TcpClientConnection(NetContext context, SocketChannel channel, SocketAddress remoteAddress) {
		super(context, channel, remoteAddress);
	}

	// for connecting channels
	static Future<TcpConnection> createConnecting(NetContext context, SocketChannel channel, SocketAddress remoteAddress, long connectTimeout)
			throws IOException {
		TcpClientConnection conn = new TcpClientConnection(context, channel, remoteAddress);
		AsyncConnectFuture<TcpConnection> future = new AsyncConnectFuture<TcpConnection>(conn);
		conn.future = future;
		conn.setupChannel();
		context.getEventDispatcher().register(conn, SelectOps.CONNECT);
		conn.setTimeout(connectTimeout);
		return future;
	}

	@Override
	public int handleOps(int ops) throws IOException {
		if (SelectOps.hasConnect(ops)) {
			// assert that only the connect event is present (no read or write)
			assert (future != null && ops == SelectOps.CONNECT);
			finishConnect();
			// stop listening for a connect timeout
			unsetTimeout();
			return events.getEventMask();
		} else {
			return super.handleOps(ops);
		}
	}

	@Override
	public void close() {
		final AsyncConnectFuture<TcpConnection> f = this.future;
		if (f != null) {
			unsetTimeout();
			f.setFailed(new IOException("connection closed while connecting"));
			future = null;
		}
		super.close();
	}

	@Override
	public boolean handleTimeout() throws IOException {
		final AsyncConnectFuture<TcpConnection> f = this.future;
		if (f == null) {
			return super.handleTimeout();
		} else {
			// connecting
			f.setFailed(new ConnectTimeoutException("connect timed out to: " + remoteAddress));
			future = null;
			return false; // close
		}
	}

	private void finishConnect() throws IOException {
		final AsyncConnectFuture<TcpConnection> f = this.future;
		this.future = null;
		try {
			boolean ok = channel.finishConnect();
			// finishConnect must throw an exception or return true since we
			// received the corresponding selector event
			assert (ok);
		} catch (IOException e) {
			context.getSessionManager().connectFailed(remoteAddress, e);
			f.setFailed(e);
			throw e;
		}
		try {
			setupSession();
		} catch (IOException e) {
			f.setFailed(e);
			throw e;
		}
		f.setFinishedOk();
	}

	@Override
	public void setEvents(Events events) {
		Validation.notNull(events);
		if (future != null) {
			// while connecting we only want to update the events
			// field and not tell the dispatcher
			this.events = events;
		} else {
			super.setEvents(events);
		}
	}
}
