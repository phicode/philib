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
package ch.bind.philib.net.core.tcp;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.net.core.Connection;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */

public final class AsyncConnectFuture<T extends Connection> implements Future<T> {

	private final T connection;

	private Exception execException;

	private boolean cancelled;

	private boolean done;

	AsyncConnectFuture(T connection) {
		Validation.notNull(connection);
		this.connection = connection;
	}

	synchronized void setFinishedOk() {
		this.done = true;
		notifyAll();
	}

	synchronized void setFailed(Exception execException) {
		this.execException = execException;
		this.done = true;
		notifyAll();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (this) {
			if (done) {
				return false;
			}
			done = true;
			cancelled = true;
		}
		SafeCloseUtil.close(connection);
		synchronized (this) {
			notifyAll();
			return true;
		}
	}

	@Override
	public synchronized boolean isCancelled() {
		return cancelled;
	}

	@Override
	public synchronized boolean isDone() {
		return done;
	}

	@Override
	public synchronized T get() throws InterruptedException, ExecutionException {
		while (!done) {
			wait();
		}
		throwWhenFailedOrCancelled();
		return connection;
	}

	@Override
	public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		long nowNs = System.nanoTime();
		long untilNs = nowNs + unit.toNanos(timeout);
		while (!done && nowNs < untilNs) {
			TimeUnit.NANOSECONDS.timedWait(this, untilNs - nowNs);
			nowNs = System.nanoTime();
		}
		if (!done) {
			throw new TimeoutException();
		}
		throwWhenFailedOrCancelled();
		return connection;
	}

	private void throwWhenFailedOrCancelled() throws ExecutionException, CancellationException {
		if (execException != null) {
			throw new ExecutionException("async connect failed", execException);
		}
		if (cancelled) {
			throw new CancellationException();
		}
	}
}
