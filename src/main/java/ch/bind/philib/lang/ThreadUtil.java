/*
 * Copyright (c) 2011 Philipp Meinen <philipp@bind.ch>
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

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.validation.SimpleValidation;

public final class ThreadUtil {

	private static final PhiLog LOG = new PhiLog(ThreadUtil.class);

	public static final long DEFAULT_WAIT_TIME_MS = 1000L;

	private ThreadUtil() {
	}

	public static boolean interruptAndJoin(Thread t) {
		return interruptAndJoin(t, DEFAULT_WAIT_TIME_MS);
	}

	public static boolean interruptAndJoin(Thread t, long waitTime) {
		if (t == null)
			return true;
		if (!t.isAlive())
			return true;

		t.interrupt();
		try {
			t.join(waitTime);
		} catch (InterruptedException e) {
			LOG.warn("interrupted while waiting for a thread to finish: " + e.getMessage(), e);
		}
		if (t.isAlive()) {
			LOG.warn("thread is still alive: " + t.getName());
			return false;
		} else {
			return true;
		}
	}

	private static final AtomicLong FOREVER_RUNNER_SEQ = new AtomicLong(1);

	private static final String FOREVER_RUNNER_NAME_FMT = "%s-for-%s-%d";

	// TODO: documentation
	public static Thread runForever(Runnable runnable) {
		SimpleValidation.notNull(runnable);
		String threadName = String.format(FOREVER_RUNNER_NAME_FMT, //
				ForeverRunner.class.getSimpleName(), runnable.getClass().getSimpleName(), FOREVER_RUNNER_SEQ.getAndIncrement());
		return runForever(runnable, threadName);
	}

	// TODO: documentation
	public static Thread runForever(Runnable runnable, String threadName) {
		return runForever(null, runnable, threadName);
	}

	// TODO: documentation
	public static Thread runForever(ThreadGroup group, Runnable runnable, String threadName) {
		// stackSize of 0 stands for: ignore this parameter
		return runForever(null, runnable, threadName, 0);
	}

	// TODO: documentation
	public static Thread runForever(ThreadGroup group, Runnable runnable, String threadName, long stackSize) {
		SimpleValidation.notNull(runnable);
		SimpleValidation.notNull(threadName);
		ForeverRunner runner = new ForeverRunner(threadName, runnable);
		Thread t = new Thread(group, runner, threadName, stackSize);
		t.start();
		return t;
	}

	private static final class ForeverRunner implements Runnable {

		private final String threadName;

		private final Runnable runnable;

		public ForeverRunner(String threadName, Runnable runnable) {
			this.threadName = threadName;
			this.runnable = runnable;
		}

		@Override
		public void run() {
			while (true) {
				try {
					runnable.run();
				} catch (Exception e) {
					if (e instanceof InterruptedException) {
						// only shut down when we have a lawfull shutdown
						return;
					} else {
						LOG.info("runnable crashed, restarting it. thread-name=" + threadName, e);
					}
				}
			}
		}
	}
}
