/*
 * Copyright (c) 2015 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * SingleFlight implements call deduplication for equal keys.
 * <p/>
 * Example:
 * <pre>
 * public Result expensiveOperation(final Parameters parameters) throws Exception {
 *     return singleFlight.execute(parameters, new Callable&lt;Result&gt;() {
 *         &#64;Override
 *         public Result call() {
 *             return expensiveOperationImpl(parameters);
 *         }
 *     });
 * }
 *
 * private Result expensiveOperationImpl(Parameters parameters) {
 *     // the real implementation
 * }
 * </pre>
 */
public class SingleFlight {

	private final ConcurrentMap<Object, Call> calls = new ConcurrentHashMap<>();

	/**
	 * Execute a {@link Callable} if no other calls for the same  {@code key} are currently running.
	 * Concurrent calls for the same  {@code key} result in one caller invoking the {@link Callable} and sharing the result
	 * with the other callers.
	 * <p/>
	 * The result of an invocation is not cached, only concurrent calls share the same result.
	 *
	 * @param key      A unique identification of the method call.
	 *                 The {@code key} must be uniquely identifiable by it's {@link Object#hashCode()} and {@link Object#equals(Object)} methods.
	 * @param callable The {@link Callable} where the result can be obtained from.
	 * @return The result of invoking the {@link Callable}.
	 * @throws Exception The {@link Exception} which was thrown by the {@link Callable}.
	 *                   Alternatively a {@link InterruptedException} can be thrown if
	 *                   the executing {@link Thread} was interrupted while waiting for the result.
	 */
	@SuppressWarnings("unchecked")
	public <V> V execute(Object key, Callable<V> callable) throws Exception {
		Call<V> call = calls.get(key);
		if (call == null) {
			call = new Call<>();
			Call<V> other = calls.putIfAbsent(key, call);
			if (other == null) {
				try {
					return call.exec(callable);
				} finally {
					calls.remove(key);
				}
			} else {
				call = other;
			}
		}
		return call.await();
	}

	private static class Call<V> {

		private final Object lock = new Object();
		private boolean finished;
		private V result;
		private Exception exc;

		void finished(V result, Exception exc) {
			synchronized (lock) {
				this.finished = true;
				this.result = result;
				this.exc = exc;
				lock.notifyAll();
			}
		}

		V await() throws Exception {
			synchronized (lock) {
				while (!finished) {
					lock.wait();
				}
				if (exc != null) {
					throw exc;
				}
				return result;
			}
		}

		V exec(Callable<V> callable) throws Exception {
			V result = null;
			Exception exc = null;
			try {
				result = callable.call();
				return result;
			} catch (Exception e) {
				exc = e;
				throw e;
			} finally {
				finished(result, exc);
			}
		}
	}
}
