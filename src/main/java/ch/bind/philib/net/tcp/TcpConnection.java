/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.bind.philib.io.BitOps;
import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandler;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class TcpConnection extends TcpConnectionBase {

	private TcpConnection(NetContext context, SocketChannel channel) {
		super(context, channel);
	}

	static Session create(NetContext context, SocketChannel channel, SessionFactory sessionFactory) throws IOException {
		TcpConnection connection = new TcpConnection(context, channel);
		return connection.setup(sessionFactory);
	}

	public static Session syncOpen(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory)
			throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(true);
		context.setSocketOptions(channel.socket());
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}

		return create(context, channel, sessionFactory);
	}

	public static Future<Session> asyncOpen(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory)
			throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());

		boolean finished = channel.connect(endpoint);
		if (finished) {
			Session session = create(context, channel, sessionFactory);
			return AsyncConnect.forFinishedConnect(session);
		} else {
			return AsyncConnect.forPendingConnect(context, channel, sessionFactory);
		}
	}

	public static Future<Session> asyncOpen(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public String getDebugInformations() {
		return "none";
	}

	private static class AsyncConnect extends EventHandlerBase implements Future<Session> {

		private SocketChannel channel;

		private SessionFactory sessionFactory;

		private Session session;

		private Exception execException;

		private boolean cancelled;

		private AsyncConnect(NetContext context, Session session) {
			super(context);
			this.session = session;
		}

		private AsyncConnect(NetContext context, SocketChannel channel, SessionFactory sessionFactory) {
			super(context);
			Validation.notNull(channel);
			Validation.notNull(sessionFactory);
			this.channel = channel;
			this.sessionFactory = sessionFactory;
		}

		public static Future<Session> forPendingConnect(NetContext context, SocketChannel channel, SessionFactory sessionFactory) {
			AsyncConnect asyncConnect = new AsyncConnect(context, channel, sessionFactory);
			context.getEventDispatcher().register(asyncConnect, EventUtil.CONNECT);
			return asyncConnect;
		}

		static AsyncConnect forFinishedConnect(NetContext context, Session session) {
			return new AsyncConnect(context, session);
		}

		@Override
		public synchronized boolean cancel(boolean mayInterruptIfRunning) {
			if (session != null) {
				return false;
			}
			cancelled = true;
			SafeCloseUtil.close(this);
			return true;
		}

		@Override
		public synchronized boolean isCancelled() {
			return cancelled;
		}

		@Override
		public synchronized boolean isDone() {
			return (session != null || execException != null || cancelled);
		}

		@Override
		public synchronized Session get() throws InterruptedException, ExecutionException {
			while (session == null && execException == null && !cancelled) {
				wait();
			}
			if (execException != null) {
				throw new ExecutionException("connect failed", execException);
			}
			if (cancelled) {
				throw new CancellationException();
			}
			return session;
		}

		@Override
		public synchronized Session get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			// TODO
			throw new UnsupportedOperationException("TODO");
		}

		@Override
		public synchronized void close() throws IOException {
			asdfasdfasdf make get reliable
			if (context != null && channel != null) {
				context.getEventDispatcher().unregister(this);
				sessionFactory = null;
			}
			SafeCloseUtil.close(channel);
			channel = null;
			notifyAll();
		}

		@Override
		public SelectableChannel getChannel() {
			// no synchronization needed because of the visibility guarantees of constructors
			return channel;
		}

		@Override
		public synchronized int handle(int ops) throws IOException {
			Validation.isTrue(ops == EventUtil.CONNECT);
			this.session = TcpConnection.create(context, channel, sessionFactory);
			notifyAll();
			context.getEventDispatcher().unregister(this);
			return EventUtil.CONNECT;
		}
	}
}
