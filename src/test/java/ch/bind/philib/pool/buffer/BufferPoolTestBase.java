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

package ch.bind.philib.pool.buffer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.concurrent.Semaphore;

import org.testng.annotations.Test;

import ch.bind.philib.TestUtil;
import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.PoolStats;
import ch.bind.philib.validation.Validation;

@Test(singleThreaded = true)
public abstract class BufferPoolTestBase<T> {

	abstract Pool<T> createPool(int bufferSize, int maxEntries);

	abstract T createBuffer(int bufferSize);

	@Test
	public void testMaxEntries() {
		int maxEntries = 1;
		int bufferSize = 8192;
		Pool<T> pool = createPool(bufferSize, maxEntries);

		// create 2 elements
		T in1 = createBuffer(bufferSize);
		T in2 = createBuffer(bufferSize);

		// the cache must accept this one because it is empty
		pool.recycle(in1);
		// but it must ignore this entry because it is full
		pool.recycle(in2);

		T out1 = pool.take();
		T out2 = pool.take();

		// out1 should be in1
		// out2 should have been newly created
		assertTrue(out1 == in1);
		assertTrue(out2 != in2);
	}

	@Test
	public void useAnyBuffers() {
		int maxEntries = 10;
		int bufferSize = 8192;
		Pool<T> pool = createPool(bufferSize, maxEntries);
		T in = createBuffer(bufferSize);

		pool.recycle(in);

		T out = pool.take();
		T outNew = pool.take();

		assertTrue(in == out);
		assertTrue(in != outNew);
	}

	@Test
	public void onlyTakeCorrectSizeBuffers() {
		int maxEntries = 10;
		int bufferSize = 8192;
		Pool<T> pool = createPool(bufferSize, maxEntries);
		T b = createBuffer(bufferSize / 2);

		pool.recycle(b);

		T b2 = pool.take();

		assertTrue(b != b2);
	}

	@Test
	public void clear() {
		int maxEntries = 10;
		int bufferSize = 8192;
		Pool<T> pool = createPool(bufferSize, maxEntries);
		for (int i = 0; i < maxEntries * 2; i++) {
			pool.recycle(createBuffer(bufferSize));
		}
		assertTrue(pool.getNumPooled() > 0);

		pool.clear();

		assertEquals(pool.getNumPooled(), 0);
	}

	@Test
	public void stats() {
		int maxEntries = 10;
		int bufferSize = 8192;
		Pool<T> pool = createPool(bufferSize, maxEntries);
		PoolStats stats = pool.getPoolStats();

		assertEquals(stats.getCreates(), 0);
		assertEquals(stats.getRecycled(), 0);
		assertEquals(stats.getReleased(), 0);
		assertEquals(stats.getTakes(), 0);

		String strStatsA = stats.toString();
		assertNotNull(strStatsA);

		assertNotNull(pool.take());

		String strStatsB = stats.toString();
		assertNotNull(strStatsB);
		assertNotEquals(strStatsA, strStatsB);
		strStatsA = strStatsB;

		pool.recycle(createBuffer(bufferSize));

		strStatsB = stats.toString();
		assertNotNull(strStatsB);
		assertNotEquals(strStatsA, strStatsB);
		strStatsA = strStatsB;
	}

	@Test
	public void benchmark() throws Exception {
		if (!TestUtil.RUN_BENCHMARKS) {
			return;
		}
		long numOps = 32L * 1024L * 1024L;
		int getOps = 1000;
		int putOps = 950;
		int numRuns = 1;
		for (int i = 1; i <= 16; i *= 2) {
			for (int r = 0; r < numRuns; r++) {
				benchmark(i, numOps, getOps, putOps);
			}
		}
	}

	public void benchmark(int numThreads, long numOps, int getOps, int putOps) throws Exception {
		TestUtil.gcAndSleep();

		int totalbufferSize = 32 * 1024 * 1024;
		int bufferSize = 1024;
		int maxEntries = totalbufferSize / bufferSize;

		Pool<T> pool = createPool(bufferSize, maxEntries);
		Thread[] ts = new Thread[numThreads];
		long numOpsPerThread = numOps / numThreads;
		Semaphore ready = new Semaphore(0);
		Semaphore go = new Semaphore(0);
		Semaphore end = new Semaphore(0);
		for (int i = 0; i < numThreads; i++) {
			StressTester<T> st = new StressTester<T>(ready, go, end, pool, numOpsPerThread, getOps, putOps);
			ts[i] = new Thread(st);
			ts[i].start();
		}
		// wait until all threads are started and waiting on the semaphore
		ready.acquire(numThreads);
		long tStartNs = System.nanoTime();
		// let the threads run
		go.release(numThreads);
		// wait again until all threads have finished
		end.acquire(numThreads);
		long timeNs = System.nanoTime() - tStartNs;
		for (Thread t : ts) {
			t.join();
		}
		pool.clear();
		TestUtil.printBenchResults(getClass(), "ops " + numThreads + " threads", "ops", timeNs, numOps);
	}

	private static final class StressTester<T> implements Runnable {

		private final Semaphore ready;

		private final Semaphore go;

		private final Semaphore end;

		private final Pool<T> pool;

		private final long numOps;

		private final int getOps;

		private final int putOps;

		public StressTester(Semaphore ready, Semaphore go, Semaphore end, Pool<T> pool, long numOps, int getOps, int putOps) {
			Validation.isTrue(getOps >= putOps);
			this.ready = ready;
			this.go = go;
			this.end = end;
			this.pool = pool;
			this.numOps = numOps;
			this.getOps = getOps;
			this.putOps = putOps;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			ready.release();
			try {
				go.acquire();
				long ops = 0;
				Object[] bufs = new Object[getOps];
				while (ops < numOps) {
					for (int i = 0; i < getOps; i++) {
						T b = pool.take();
						assertNotNull(b);
						bufs[i] = b;
					}
					for (int i = 0; i < putOps; i++) {
						assertNotNull(bufs[i]);
						pool.recycle((T) bufs[i]);
					}
					ops += getOps;
					ops += putOps;
				}
			} catch (InterruptedException e) {
				fail("test was interrupted");
			} finally {
				end.release();
			}
		}
	}
}
