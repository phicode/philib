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

import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class SingleFlightTest {

	@Test(timeOut = 1000)
	public void sequential() throws Exception {
		Callable<String> a = callableString("a");
		Callable<String> b = callableString("b");
		Callable<String> c = callableString("c");
		SingleFlight sf = new SingleFlight();
		assertEquals(sf.execute("same-key", a), "a");
		assertEquals(sf.execute("same-key", b), "b");
		assertEquals(sf.execute("same-key", c), "c");
	}

	@Test(timeOut = 1000)
	public void sequentialException() {
		Callable<String> a = callableException("a");
		Callable<String> b = callableException("b");
		SingleFlight sf = new SingleFlight();
		try {
			sf.execute("same-key", a);
			fail("should have thrown an exception");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "a");
		}
		try {
			sf.execute("same-key", b);
			fail("should have thrown an exception");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "b");
		}
	}

	@Test(timeOut = 1000)
	public void regularDeduplicate() throws Exception {
		Callable<String> a = delay(callableString("a"), 200);
		Callable<String> b = callableString("b");

		SingleFlight sf = new SingleFlight();

		AsyncResult<String> resultA = async(sf, "same-key", a);

		// make sure that the first (delay) task has acquired the lock for the supplied key
		Thread.sleep(100);

		AsyncResult<String> resultB = async(sf, "same-key", b);

		resultA.thread.join();
		resultB.thread.join();

		assertEquals(resultA.result, "a");
		// in reality the same key must yield the same result for this pattern to make sense.
		// for this test the second callable would return "b" to find errors in the implementation
		// since single-flight must deduplicate these calls both results must be "a"
		assertEquals(resultB.result, "a");

		assertNull(resultA.exc);
		assertNull(resultB.exc);
	}

	@Test(timeOut = 1000)
	public void deduplicateWithException() throws Exception {
		Callable<String> a = delay(callableException("a"), 200);
		Callable<String> b = callableException("b");

		SingleFlight sf = new SingleFlight();

		AsyncResult<String> resultA = async(sf, "same-key", a);
		Thread.sleep(100);
		AsyncResult<String> resultB = async(sf, "same-key", b);

		resultA.thread.join();
		resultB.thread.join();

		// see the comments in other unit tests
		assertEquals(resultA.exc.getMessage(), "a");
		assertEquals(resultB.exc.getMessage(), "a");

		assertNull(resultA.result);
		assertNull(resultB.result);
	}

	@Test(timeOut = 1000)
	public void differentKeys() throws InterruptedException {
		final AtomicInteger numCalls = new AtomicInteger();
		Callable<Integer> c = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return numCalls.incrementAndGet();
			}
		};
		c = delay(c, 100);
		SingleFlight sf = new SingleFlight();
		AsyncResult<Integer> result1 = async(sf, "key-1", c);
		AsyncResult<Integer> result2 = async(sf, "key-2", c);
		result1.thread.join();
		result2.thread.join();

		assertEquals(numCalls.get(), 2);
		assertTrue((result1.result == 1 && result2.result == 2)
				|| (result1.result == 2 && result2.result == 1));
	}

	private Callable<String> callableString(final String value) {
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				return value;
			}
		};
	}

	private Callable<String> callableException(final String message) {
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				throw new Exception(message);
			}
		};
	}

	private <V> Callable<V> delay(final Callable<V> delegate, final long delay) {
		return new Callable<V>() {
			@Override
			public V call() throws Exception {
				Thread.sleep(delay);
				return delegate.call();
			}
		};
	}

	private <V> AsyncResult<V> async(SingleFlight sf, Object key, Callable<V> callable) {
		AsyncResult<V> result = new AsyncResult<>(sf, key, callable);
		result.start();
		return result;
	}

	static class AsyncResult<V> implements Runnable {
		final SingleFlight singleFlight;
		final Object key;
		final Callable<V> callable;

		V result;
		Exception exc;
		Thread thread;

		public AsyncResult(SingleFlight singleFlight, Object key, Callable<V> callable) {
			this.singleFlight = singleFlight;
			this.key = key;
			this.callable = callable;
		}

		@Override
		public void run() {
			try {
				result = singleFlight.execute(key, callable);
			} catch (Exception e) {
				exc = e;
			}
		}

		public void start() {
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}
}
