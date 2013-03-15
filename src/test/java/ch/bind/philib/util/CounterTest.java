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

package ch.bind.philib.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.concurrent.CountDownLatch;

import org.testng.annotations.Test;

import ch.bind.philib.TestUtil;

public class CounterTest {

	private static final String EXPECT_ZERO = "a[counts=0, total=0, min=N/A, max=N/A, avg=N/A]";

	@Test
	public void zero() {
		Counter pm = new Counter("a");
		assertEquals(pm.toString(), EXPECT_ZERO);
	}

	@Test
	public void one() {
		Counter pm = new Counter("a");
		pm.count(500);
		assertEquals(pm.toString(), "a[counts=1, total=500, min=500, max=500, avg=500.000]");
	}

	@Test
	public void two() {
		Counter pm = new Counter("a");
		pm.count(100);
		pm.count(500);
		assertEquals(pm.toString(), "a[counts=2, total=600, min=100, max=500, avg=300.000]");
	}

	@Test
	public void three() {
		Counter pm = new Counter("a");
		pm.count(200);
		pm.count(100);
		pm.count(300);
		assertEquals(pm.toString(), "a[counts=3, total=600, min=100, max=300, avg=200.000]");
	}

	@Test
	public void many() {
		Counter pm = new Counter("a");
		for (int i = 1000; i <= 10000; i++) {
			pm.count(i);
		}
		for (int i = 999; i >= -100; i--) {
			pm.count(i);
		}
		assertEquals(pm.toString(), "a[counts=10001, total=50005000, min=0, max=10000, avg=5000.000]");
	}

	@Test
	public void reset() {
		Counter pm = new Counter("a");
		assertEquals(pm.toString(), EXPECT_ZERO);
		pm.count(100);
		assertEquals(pm.toString(), "a[counts=1, total=100, min=100, max=100, avg=100.000]");
		pm.reset();
		assertEquals(pm.toString(), EXPECT_ZERO);
	}

	@Test
	public void parallel() throws Exception {
		if (!TestUtil.RUN_BENCHMARKS) {
			return;
		}
		// warmup
		for (int p = 1; p <= 16; p++) {
			parallel(1000 * 1000, p);
		}
		// for real
		for (int p = 1; p <= 16; p++) {
			parallel(100 * 1000 * 1000, p);
		}
	}

	private void parallel(int N, int p) throws Exception {
		Counter counter = new Counter("parallel");
		int n = N / p;
		CountDownLatch ready = new CountDownLatch(p);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(p);

		for (int i = 0; i < p; i++) {
			Runnable r = new C(counter, n, ready, start, finished);
			Thread t = new Thread(r, getClass().getSimpleName() + "-parallel-" + i);
			t.start();
		}

		ready.await();
		long t0 = System.nanoTime();
		start.countDown();
		finished.await();
		long t1 = System.nanoTime();
		TestUtil.printBenchResults(getClass(), "count-parallel-" + p, "count", t1 - t0, N);
	}

	private static final class C implements Runnable {
		final Counter counter;

		final int n;

		final CountDownLatch ready;

		final CountDownLatch start;

		final CountDownLatch finished;

		C(Counter counter, int n, CountDownLatch ready, CountDownLatch start, CountDownLatch finished) {
			this.counter = counter;
			this.n = n;
			this.ready = ready;
			this.start = start;
			this.finished = finished;
		}

		@Override
		public void run() {
			ready.countDown();
			try {
				start.await();
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
			for (int i = 0; i < n; i++) {
				counter.count(i);
			}
			finished.countDown();
		}
	}
}
