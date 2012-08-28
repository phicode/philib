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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */

public final class AsyncConnectHandler extends EventHandlerBase implements Future<Session> {

	private SocketChannel channel;

	private SessionFactory sessionFactory;

	private Session session;

	private Exception execException;

	private boolean cancelled;

	private boolean registered;

	private AsyncConnectHandler(NetContext context, SocketChannel channel, SessionFactory sessionFactory) {
		super(context);
		Validation.notNull(channel);
		Validation.notNull(sessionFactory);
		this.channel = channel;
		this.sessionFactory = sessionFactory;
	}

	public static AsyncConnectHandler create(NetContext context, SocketChannel channel, SessionFactory sessionFactory) {
		AsyncConnectHandler rv = new AsyncConnectHandler(context, channel, sessionFactory);
		rv.context.getEventDispatcher().register(rv, EventUtil.CONNECT);
		rv.registered = true;
		return rv;
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
		return _isDone();
	}

	private boolean _isDone() {
		return session != null || execException != null || cancelled;
	}

	@Override
	public synchronized Session get() throws InterruptedException, ExecutionException {
		while (!_isDone()) {
			wait();
		}
		if (execException != null) {
			throw new ExecutionException("async connect failed", execException);
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
		// TODO: make get reliable
		if (context != null && channel != null) {
			if (registered) {
				context.getEventDispatcher().unregister(this);
				registered = false;
			}
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
	public synchronized int handle(int ops) {
		Validation.isTrue(ops == EventUtil.CONNECT);
		if (execException != null || cancelled) {
			SafeCloseUtil.close(this);
		} else {
			try {
				if (channel.finishConnect()) {
					this.session = TcpNetFactory.create(this, context, channel, sessionFactory);
					registered = false;
					// creating a tcp-connection has changed the interested-ops and handler-attachment of the
					// registration-key. this async connect handler is no longer registered and must tell the event
					// handler that it does not want to overwrite its interested-ops
					notifyAll();
					return EventUtil.OP_DONT_CHANGE;
				}
			} catch (IOException e) {
				execException = e;
				SafeCloseUtil.close(channel);
			}
		}
		notifyAll();
		return EventUtil.CONNECT;
	}
}
