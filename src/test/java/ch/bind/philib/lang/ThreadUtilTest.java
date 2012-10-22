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

	// slower machines require a large amount of time to load all the classes
	// which are pulled in by this test, so the timeout needs to be way higher
	// then on faster machines
	@Test(timeOut = 2000)
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
