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

package ch.bind.philib.cache.buffercache;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.Semaphore;

import org.testng.annotations.Test;

import ch.bind.philib.TestUtil;
import ch.bind.philib.validation.Validation;

public class ByteArrayCacheTest {

	@Test
	public void testMaxEntries() {
		int maxEntries = 1;
		int bufferSize = ByteArrayCache.DEFAULT_BUFFER_SIZE;
		ByteArrayCache bp = ByteArrayCache.createSimple(bufferSize, maxEntries);

		byte[] in1 = new byte[bufferSize];
		byte[] in2 = new byte[bufferSize];

		// the cache must accept this one because it is empty
		bp.release(in1);
		// but it must ignore this entry because it is full
		bp.release(in2);

		byte[] out1 = bp.acquire();
		byte[] out2 = bp.acquire();

		// out1 should be in1
		// out2 should have been newly created
		assertTrue(out1 == in1);
		assertTrue(out2 != in2);
	}

	@Test
	public void useAnyBuffers() {
		int bufferSize = ByteArrayCache.DEFAULT_BUFFER_SIZE;
		ByteArrayCache bp = ByteArrayCache.createSimple(bufferSize);
		byte[] in = new byte[bufferSize];

		bp.release(in);

		byte[] out = bp.acquire();
		byte[] outNew = bp.acquire();

		assertTrue(in == out);
		assertTrue(in != outNew);
	}

	@Test
	public void onlyTakeCorrectSizeBuffers() {
		int bufferSize = ByteArrayCache.DEFAULT_BUFFER_SIZE;
		ByteArrayCache bp = ByteArrayCache.createSimple(bufferSize);
		byte[] b = new byte[bufferSize / 2];

		bp.release(b);

		byte[] b2 = bp.acquire();

		assertTrue(b != b2);
	}

	private static final boolean printResults = true;

	// not really a unit test, only enable this while developing on the code
	// @Test
	public void stressTest() throws Exception {
		// stressTest(true);
		stressTest(0);
		stressTest(1);
		stressTest(2);
		stressTest(3);
	}

	public void stressTest(int type) throws Exception {
		// long numOps = 32L * 1024L * 1024L;
		long numOps = 8L * 1024L * 1024L;
		// long numOps = 2L * 1024L * 1024L;
		// release fewer then we get -> the buffer cache has to create new
		// objects
		// int getOps = 100;
		// int putOps = 99;
		int getOps = 1000;
		int putOps = 1000;
		// int getOps = 5;
		// int putOps = 4;
		boolean firstRun = true;
		int numRuns = 1;
		for (int i = 1; i <= 32; i *= 2) {
			// long total = 0;
			for (int r = 0; r < numRuns; r++) {
				// total +=
				stressTest(type, firstRun, i, numOps, getOps, putOps);
				firstRun = false;
			}
			// long average = total / numRuns;
			// System.out.printf("threads=%d, average-time=%d%n", i, average);
		}
	}

	public long stressTest(int type, boolean firstRun, int numThreads, long numOps, int getOps, int putOps) throws Exception {
		TestUtil.gcAndSleep();

		int totalbufferSize = 32 * 1024 * 1024;
		int bufferSize = 16;
		int numBufs = totalbufferSize / bufferSize / (4096 / 16);
		// System.out.printf("using at most %d buffers%n", numBufs);
		ByteArrayCache bp;
		String bpName;
		switch (type) {
		case 0:
			bp = ByteArrayCache.createSimple(bufferSize, numBufs);
			bpName = "normal";
			break;
		case 1:
			bp = ByteArrayCache.createScalable(bufferSize, numBufs, 4);
			bpName = "scalable-4";
			break;
		case 2:
			bp = ByteArrayCache.createScalable(bufferSize, numBufs, 32);
			bpName = "scalable-32";
			break;
		case 3:
			bp = ByteArrayCache.createNoop(bufferSize);
			bpName = "null";
			break;
		default:
			throw new AssertionError();
		}
		Thread[] ts = new Thread[numThreads];
		long numOpsPerThread = numOps / numThreads;
		Semaphore ready = new Semaphore(0);
		Semaphore go = new Semaphore(0);
		Semaphore end = new Semaphore(0);
		for (int i = 0; i < numThreads; i++) {
			StressTester st = new StressTester(ready, go, end, bp, numOpsPerThread, getOps, putOps);
			ts[i] = new Thread(st);
			ts[i].start();
		}
		// wait until all threads are started and waiting on the semaphore
		ready.acquire(numThreads);
		long tStart = System.currentTimeMillis();
		// let the threads run
		go.release(numThreads);
		// wait again until all threads have finished
		end.acquire(numThreads);
		long tEnd = System.currentTimeMillis();
		for (Thread t : ts) {
			t.join();
		}
		long time = tEnd - tStart;

		double opsPerMs = numOps / ((double) time);
		if (printResults) {
			// long bufsCreated = bp.getNumCreates();
			long bufsCreated = 0;
			if (firstRun) {
				System.out.println(bpName + " #threads; #ops ; #new bufs ; time(ms); ops/msec");
			}
			System.out.printf("%2d ; %9d ; %9d ; %4d ; %.3f%n", numThreads, numOps, bufsCreated, time, opsPerMs);

			if (numThreads == 32) {
				// bp.printUseCounts();
			}
		}
		return time;
	}

	private static final class StressTester implements Runnable {

		private final Semaphore ready;

		private final Semaphore go;

		private final Semaphore end;

		private final ByteArrayCache bp;

		private final long numOps;

		private final int getOps;

		private final int putOps;

		public StressTester(Semaphore ready, Semaphore go, Semaphore end, ByteArrayCache bp, long numOps, int getOps, int putOps) {
			Validation.isTrue(getOps >= putOps);
			this.ready = ready;
			this.go = go;
			this.end = end;
			this.bp = bp;
			this.numOps = numOps;
			this.getOps = getOps;
			this.putOps = putOps;
		}

		@Override
		public void run() {
			ready.release();
			try {
				go.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
				end.release();
			}
			try {
				long ops = 0;
				byte[][] bufs = new byte[getOps][];
				while (ops < numOps) {
					for (int i = 0; i < getOps; i++) {
						byte[] b = bp.acquire();
						assertNotNull(b);
						bufs[i] = b;
					}
					for (int i = 0; i < putOps; i++) {
						assertNotNull(bufs[i]);
						bp.release(bufs[i]);
					}
					ops += getOps;
					ops += putOps;
				}
			} finally {
				end.release();
			}
		}
	}
}
