package ch.bind.philib.lang;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class ThreadUtilTest {

	@Test(timeOut = 1000)
	public void normalShutdown() throws Exception {
		DelayRunnable r = new DelayRunnable(100, 0);
		Thread t = ThreadUtil.createAndStartForeverRunner(r);
		long start = System.currentTimeMillis();
		t.join();
		long time = System.currentTimeMillis() - start;
		assertTrue(time > 90 && time < 150);
		assertFalse(t.isAlive());
		assertEquals(r.numStarts, 1);
		assertFalse(r.wasInterrupted);
	}

	@Test(timeOut = 1000)
	public void interruptShutdown() throws Exception {
		DelayRunnable r = new DelayRunnable(5000000, 0);
		Thread t = ThreadUtil.createAndStartForeverRunner(r);
		Thread.sleep(200);
		assertTrue(t.isAlive());
		boolean ok = ThreadUtil.interruptAndJoin(t);
		assertTrue(ok);

		assertFalse(t.isAlive());
		assertEquals(r.numStarts, 1);
		assertTrue(r.wasInterrupted);
	}

	@Test(timeOut = 1000)
	public void restartFaultyThread() throws Exception {
		DelayRunnable r = new DelayRunnable(100, 5);
		Thread t = ThreadUtil.createAndStartForeverRunner(r);
		long start = System.currentTimeMillis();
		t.join(800);
		long time = System.currentTimeMillis() - start;
		assertTrue(time >= 600 && time < 750);
		assertFalse(t.isAlive());
		assertEquals(r.numStarts, 6);
		assertFalse(r.wasInterrupted);
	}

	private static final class DelayRunnable implements Runnable {

		private final long delayMs;

		private int numThrowExc;

		volatile boolean wasInterrupted;

		volatile int numStarts;

		DelayRunnable(long delayMs, int numThrowExc) {
			super();
			this.delayMs = delayMs;
			this.numThrowExc = numThrowExc;
		}

		@Override
		public void run() {
			numStarts++;
			try {
				Thread.sleep(delayMs);
				if (numThrowExc > 0) {
					numThrowExc--;
					throw new RuntimeException("forever-runner should restart this runnable");
				}
			} catch (InterruptedException e) {
				wasInterrupted = true;
				Thread.currentThread().interrupt();
			}
		}
	}
}
