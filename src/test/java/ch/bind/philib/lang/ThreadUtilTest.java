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

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ThreadUtilTest {

	@Test
	public void normal() throws Exception {
		RestartTestRunnable r = new RestartTestRunnable(false, false);
		Runnable wrapped = new ThreadUtil.ForeverRunner(r);
		wrapped.run();
		assertEquals(r.numStarts, 1);
	}

	@Test
	public void exception() throws Exception {
		RestartTestRunnable r = new RestartTestRunnable(true, false);
		Runnable wrapped = new ThreadUtil.ForeverRunner(r);
		wrapped.run();
		assertEquals(r.numStarts, 2);
	}

	@Test
	public void error() throws Exception {
		RestartTestRunnable r = new RestartTestRunnable(false, true);
		Runnable wrapped = new ThreadUtil.ForeverRunner(r);
		wrapped.run();
		assertEquals(r.numStarts, 2);
	}

	@Test
	public void exceptionThenError() throws Exception {
		RestartTestRunnable r = new RestartTestRunnable(true, true);
		Runnable wrapped = new ThreadUtil.ForeverRunner(r);
		wrapped.run();
		assertEquals(r.numStarts, 3);
	}

	@Test(timeOut = 1000)
	public void interruptAndJoinWontStopOnFirstInterrupt() throws Exception {
		final CountDownLatch started = new CountDownLatch(1);
		final CountDownLatch stopped = new CountDownLatch(1);
		Thread t = new Thread(new InterruptTestRunnable(started, stopped, 1));
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

	@Test
	public void interruptAndJoinThreadsTrueOnNull() throws Exception {
		assertTrue(ThreadUtil.interruptAndJoinThreads((Thread[]) null));
		assertTrue(ThreadUtil.interruptAndJoinThreads((Thread[]) null, 100));
		assertTrue(ThreadUtil.interruptAndJoinThreads((Collection<Thread>) null));
		assertTrue(ThreadUtil.interruptAndJoinThreads((Collection<Thread>) null, 100));
	}

	@Test
	public void interruptAndJoinThreads() throws Exception {
		final int N = 10;
		final CountDownLatch started = new CountDownLatch(N);
		final CountDownLatch stopped = new CountDownLatch(N);
		List<Thread> ts = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			Thread t = new Thread(new InterruptTestRunnable(started, stopped, 1));
			ts.add(t);
		}
		ThreadUtil.startThreads(ts);
		started.await();
		// give the other threads some time to enter sleep
		Thread.sleep(50);
		assertFalse(ThreadUtil.interruptAndJoinThreads(ts, 25));
		assertTrue(ThreadUtil.interruptAndJoinThreads(ts, 25));
		for (Thread t : ts) {
			assertFalse(t.isAlive());
		}
	}

	private static final class InterruptTestRunnable implements Runnable {

		private final CountDownLatch started;
		private final CountDownLatch stopped;

		private final int numIgnoreInterrupt;

		public InterruptTestRunnable(CountDownLatch started, CountDownLatch stopped, int numIgnoreInterrupt) {
			this.started = started;
			this.stopped = stopped;
			this.numIgnoreInterrupt = numIgnoreInterrupt;
		}

		@Override
		public void run() {
			started.countDown();
			int ignoredInterrupts = 0;
			while (true) {
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e) {
					if (ignoredInterrupts >= numIgnoreInterrupt) {
						break;
					}
					ignoredInterrupts++;
				}
			}
			stopped.countDown();
		}
	}

	private static final class RestartTestRunnable implements Runnable {

		private boolean throwException;

		private boolean throwError;

		private int numStarts;

		RestartTestRunnable(boolean throwException, boolean throwError) {
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
		long tStart = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			long tms = System.currentTimeMillis();
			ThreadUtil.sleepUntilMs(tms - 10); // noop
		}
		long elapsed = (System.nanoTime() - tStart);
		// this should finish within a few hundred microseconds
		// but that would make the test very flaky due to os-specific scheduling
		assertTrue(elapsed < 25_000_1000); // 25ms
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
