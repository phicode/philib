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

import org.testng.annotations.Test;

import ch.bind.philib.pool.Pool;

@Test
public class ByteArrayPoolTest extends BufferPoolTestBase<byte[]>{

	@Override
	Pool<byte[]> createPool(int bufferSize, int maxEntries) {
		return ByteArrayPool.create(bufferSize, maxEntries);
	}

	@Override
	byte[] createBuffer(int bufferSize) {
		return new byte[bufferSize];
	}

	

//	private static final boolean printResults = true;
//
//	// not really a unit test, only enable this while developing on the code
//	// @Test
//	public void stressTest() throws Exception {
//		// stressTest(true);
//		stressTest(0);
//		stressTest(1);
//		stressTest(2);
//		stressTest(3);
//	}
//
//	public void stressTest(int type) throws Exception {
//		// long numOps = 32L * 1024L * 1024L;
//		long numOps = 8L * 1024L * 1024L;
//		// long numOps = 2L * 1024L * 1024L;
//		// release fewer then we get -> the buffer cache has to create new
//		// objects
//		// int getOps = 100;
//		// int putOps = 99;
//		int getOps = 1000;
//		int putOps = 1000;
//		// int getOps = 5;
//		// int putOps = 4;
//		boolean firstRun = true;
//		int numRuns = 1;
//		for (int i = 1; i <= 32; i *= 2) {
//			// long total = 0;
//			for (int r = 0; r < numRuns; r++) {
//				// total +=
//				stressTest(type, firstRun, i, numOps, getOps, putOps);
//				firstRun = false;
//			}
//			// long average = total / numRuns;
//			// System.out.printf("threads=%d, average-time=%d%n", i, average);
//		}
//	}
//
//	public long stressTest(int type, boolean firstRun, int numThreads, long numOps, int getOps, int putOps) throws Exception {
//		TestUtil.gcAndSleep();
//
//		int totalbufferSize = 32 * 1024 * 1024;
//		int bufferSize = 16;
//		int numBufs = totalbufferSize / bufferSize / (4096 / 16);
//		// System.out.printf("using at most %d buffers%n", numBufs);
//		ByteArrayPool bp;
//		String bpName;
//		switch (type) {
//		case 0:
//			bp = ByteArrayPool.createSimple(bufferSize, numBufs);
//			bpName = "normal";
//			break;
//		case 1:
//			bp = ByteArrayPool.createConcurrent(bufferSize, numBufs, 4);
//			bpName = "scalable-4";
//			break;
//		case 2:
//			bp = ByteArrayPool.createConcurrent(bufferSize, numBufs, 32);
//			bpName = "scalable-32";
//			break;
//		case 3:
//			bp = ByteArrayPool.createNoop(bufferSize);
//			bpName = "null";
//			break;
//		default:
//			throw new AssertionError();
//		}
//		Thread[] ts = new Thread[numThreads];
//		long numOpsPerThread = numOps / numThreads;
//		Semaphore ready = new Semaphore(0);
//		Semaphore go = new Semaphore(0);
//		Semaphore end = new Semaphore(0);
//		for (int i = 0; i < numThreads; i++) {
//			StressTester st = new StressTester(ready, go, end, bp, numOpsPerThread, getOps, putOps);
//			ts[i] = new Thread(st);
//			ts[i].start();
//		}
//		// wait until all threads are started and waiting on the semaphore
//		ready.acquire(numThreads);
//		long tStart = System.currentTimeMillis();
//		// let the threads run
//		go.release(numThreads);
//		// wait again until all threads have finished
//		end.acquire(numThreads);
//		long tEnd = System.currentTimeMillis();
//		for (Thread t : ts) {
//			t.join();
//		}
//		long time = tEnd - tStart;
//
//		double opsPerMs = numOps / ((double) time);
//		if (printResults) {
//			// long bufsCreated = bp.getNumCreates();
//			long bufsCreated = 0;
//			if (firstRun) {
//				System.out.println(bpName + " #threads; #ops ; #new bufs ; time(ms); ops/msec");
//			}
//			System.out.printf("%2d ; %9d ; %9d ; %4d ; %.3f%n", numThreads, numOps, bufsCreated, time, opsPerMs);
//
//			if (numThreads == 32) {
//				// bp.printUseCounts();
//			}
//		}
//		return time;
//	}
//
//	private static final class StressTester implements Runnable {
//
//		private final Semaphore ready;
//
//		private final Semaphore go;
//
//		private final Semaphore end;
//
//		private final ByteArrayPool bp;
//
//		private final long numOps;
//
//		private final int getOps;
//
//		private final int putOps;
//
//		public StressTester(Semaphore ready, Semaphore go, Semaphore end, ByteArrayPool bp, long numOps, int getOps, int putOps) {
//			Validation.isTrue(getOps >= putOps);
//			this.ready = ready;
//			this.go = go;
//			this.end = end;
//			this.bp = bp;
//			this.numOps = numOps;
//			this.getOps = getOps;
//			this.putOps = putOps;
//		}
//
//		@Override
//		public void run() {
//			ready.release();
//			try {
//				go.acquire();
//				long ops = 0;
//				byte[][] bufs = new byte[getOps][];
//				while (ops < numOps) {
//					for (int i = 0; i < getOps; i++) {
//						byte[] b = bp.acquire();
//						assertNotNull(b);
//						bufs[i] = b;
//					}
//					for (int i = 0; i < putOps; i++) {
//						assertNotNull(bufs[i]);
//						bp.free(bufs[i]);
//					}
//					ops += getOps;
//					ops += putOps;
//				}
//			} catch (InterruptedException e) {
//				fail("test was interrupted");
//			} finally {
//				end.release();
//			}
//		}
//	}
}
