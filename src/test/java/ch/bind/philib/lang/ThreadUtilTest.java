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

import java.util.concurrent.CountDownLatch;

import org.testng.annotations.Test;

public class ThreadUtilTest {

	@Test
	public void normal() throws Exception {
		TestRunnable r = new TestRunnable(false, false);
		Runnable wrapped = new ThreadUtil.ForeverRunner(r);
		wrapped.run();
		assertEquals(r.numStarts, 1);
	}

	@Test
	public void exception() throws Exception {
		TestRunnable r = new TestRunnable(true, false);
		Runnable wrapped = new ThreadUtil.ForeverRunner(r);
		wrapped.run();
		assertEquals(r.numStarts, 2);
	}

	@Test
	public void error() throws Exception {
		TestRunnable r = new TestRunnable(false, true);
		Runnable wrapped = new ThreadUtil.ForeverRunner(r);
		wrapped.run();
		assertEquals(r.numStarts, 2);
	}

	@Test
	public void exceptionThenError() throws Exception {
		TestRunnable r = new TestRunnable(true, true);
		Runnable wrapped = new ThreadUtil.ForeverRunner(r);
		wrapped.run();
		assertEquals(r.numStarts, 3);
	}

	@Test(timeOut = 1000)
	public void interruptAndJoinWontStopOnFirstInterrupt() throws Exception {
		final CountDownLatch started = new CountDownLatch(1);
		final CountDownLatch stopped = new CountDownLatch(1);
		Thread t = new Thread() {

			@Override
			public void run() {
				started.countDown();
				boolean second = false;
				while (true) {
					try {
						Thread.sleep(100000);
					} catch (InterruptedException e) {
						if (second) {
							return;
						}
						second = true;
					}
					stopped.countDown();
				}
			}
		};
		t.start();
		started.await();
		// give the other thread some time to enter sleep
		Thread.sleep(50);
		assertFalse(ThreadUtil.interruptAndJoin(t, 50));
		assertTrue(ThreadUtil.interruptAndJoin(t, 50));
		// the thread is no longer alive, successive calls must always return true
		assertFalse(t.isAlive());
		for (int i = 0; i < 10; i++) {
			assertTrue(ThreadUtil.interruptAndJoin(t));
		}
	}

	@Test
	public void interruptAndJoinTrueOnNull() throws Exception {
		assertTrue(ThreadUtil.interruptAndJoin(null));
		assertTrue(ThreadUtil.interruptAndJoin(null, 100));
	}

	private static final class TestRunnable implements Runnable {

		private boolean throwException;

		private boolean throwError;

		private int numStarts;

		TestRunnable(boolean throwException, boolean throwError) {
			this.throwException = throwException;
			this.throwError = throwError;
		}

		@Override
		public void run() {
			numStarts++;
			if (throwException) {
				throwException = false;
				// forever-runner must restart this runnable
				throw new RuntimeException();
			}
			if (throwError) {
				throwError = false;
				// forever-runner must not restart this runnable
				throw new Error();
			}
		}
	}

	@Test
	public void sleepUntilMsIntoThePast() throws InterruptedException {
		// 1000 times no sleep at all
		long elapsed = 0;
		for (int i = 0; i < 1000; i++) {
			long tms = System.currentTimeMillis();
			long tns = System.nanoTime();
			ThreadUtil.sleepUntilMs(tms - 10);
			elapsed += (System.nanoTime() - tns);
		}
		// this should finish within less than 200us
		// but that would make the test very flaky
		// probably even this 5 milliseconds are too flaky
		assertTrue(elapsed < 5 * 1000 * 1000);
	}

	@Test
	public void sleepUntilRegular() throws InterruptedException {
		long start = System.currentTimeMillis();
		for (int i = 1; i <= 100; i++) {
			ThreadUtil.sleepUntilMs(start + i);
		}
		long elapsed = System.currentTimeMillis() - start;
		assertTrue(elapsed >= 100);
		// this is probably also flaky as hell due to different scheduling behaviour of different platforms. lets see how well it does. 		
		assertTrue(elapsed <= 125);
	}
}
