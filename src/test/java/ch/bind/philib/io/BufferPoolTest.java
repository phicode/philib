package ch.bind.philib.io;

import java.io.IOException;

import org.junit.Test;
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

	@Test
	public void stressTest() throws Exception {
		stressTest(2);
		stressTest(4);
		stressTest(8);
		stressTest(16);
		stressTest(32);
	}

	public void stressTest(int numThreads) throws Exception {
		int totalBufSize = 32 * 1024 * 1024;
		int bufSize = 4096;
		int numBufs = totalBufSize / bufSize;
		BufferPool bp = new BufferPool(bufSize, numBufs);
		Thread[] ts = new Thread[numThreads];

		for (int i = 0; i < numThreads; i++) {
			StressTester st = new StressTester(bp);
			ts[i] = new Thread(st);
		}
		for (Thread t : ts) {
			t.start();
		}
		for (Thread t : ts) {
			t.join();
		}
	}

	private static final class StressTester implements Runnable {

		private final BufferPool bp;

		public StressTester(BufferPool bp) {
			this.bp = bp;
		}

		@Override
		public void run() {
			byte[] b = bp.get();

		}
	}
}
