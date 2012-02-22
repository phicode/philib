package ch.bind.philib.io;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.junit.Test;

import ch.bind.philib.TestUtil;
import ch.bind.philib.validation.SimpleValidation;
import static org.junit.Assert.*;

public class BufferPoolTest {

	@Test
	public void testMaxEntries() {
		int maxEntries = 1;
		int bufSize = BufferPool.DEFAULT_BUFFER_SIZE;
		BufferPool bp = new BufferPool(bufSize, maxEntries);

		byte[] in1 = new byte[bufSize];
		byte[] in2 = new byte[bufSize];

		// the pool must accept this one because it is empty
		bp.release(in1);
		// but it must ignore this entry because it is full
		bp.release(in2);

		byte[] out1 = bp.get();
		byte[] out2 = bp.get();

		// out1 should be in1
		// out2 should have been newly created
		assertTrue(out1 == in1);
		assertTrue(out2 != in2);
	}

	@Test
	public void useAnyBuffers() {
		int bufSize = BufferPool.DEFAULT_BUFFER_SIZE;
		BufferPool bp = new BufferPool(bufSize);
		byte[] in = new byte[bufSize];

		bp.release(in);

		byte[] out = bp.get();
		byte[] outNew = bp.get();

		assertTrue(in == out);
		assertTrue(in != outNew);
	}

	@Test
	public void onlyTakeCorrectSizeBuffers() {
		int bufSize = BufferPool.DEFAULT_BUFFER_SIZE;
		BufferPool bp = new BufferPool(bufSize);
		byte[] b = new byte[bufSize / 2];

		bp.release(b);

		byte[] b2 = bp.get();

		assertTrue(b != b2);
	}

	private static final boolean printResults = true;

	@Test
	public void stressTest() throws Exception {
		long numOps = 32L * 1024L * 1024L;
//	    long numOps = 2L * 1024L * 1024L;
		// release fewer then we get -> the buffer pool has to create new
		// objects
		// int getOps = 100;
		// int putOps = 99;
		int getOps = 5;
		int putOps = 4;
		boolean firstRun = true;
		int numRuns = 1;
		for (int i = 1; i <= 32; i *= 2) {
			long total = 0;
			for (int r = 0; r < numRuns; r++) {
				total += stressTest(firstRun, i, numOps, getOps, putOps);
				firstRun = false;
			}
			long average = total / numRuns;
			// System.out.printf("threads=%d, average-time=%d%n", i, average);
		}
	}

	public long stressTest(boolean firstRun, int numThreads, long numOps, int getOps, int putOps) throws Exception {
		TestUtil.gcAndSleep();

		int totalBufSize = 32 * 1024 * 1024;
		int bufSize = 16;
		int numBufs = totalBufSize / bufSize / (4096 / 16);
		BufferPool bp = new BufferPool(bufSize, numBufs);
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
			long bufsCreated = bp.getNumCreates();
			if (firstRun) {
				System.out.println("#threads; #ops ; #new bufs ; time(ms); ops/msec");
			}
			System.out.printf("%2d ; %9d ; %9d ; %4d ; %.3f%n", numThreads, numOps, bufsCreated, time, opsPerMs);
		}
		return time;
	}

	private static final class StressTester implements Runnable {

		private final Semaphore ready;

		private final Semaphore go;

		private final Semaphore end;

		private final BufferPool bp;

		private final long numOps;

		private final int getOps;

		private final int putOps;

		public StressTester(Semaphore ready, Semaphore go, Semaphore end, BufferPool bp, long numOps, int getOps, int putOps) {
			SimpleValidation.isTrue(getOps >= putOps);
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
			}
			try {
				long ops = 0;
				byte[][] bufs = new byte[getOps][];
				while (ops < numOps) {
					for (int i = 0; i < getOps; i++) {
						byte[] b = bp.get();
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
