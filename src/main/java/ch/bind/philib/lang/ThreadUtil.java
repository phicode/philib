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

import org.mockito.internal.stubbing.answers.ThrowsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public abstract class ThreadUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ThreadUtil.class);

	protected ThreadUtil() {}

	public static void sleepUntilMs(long time) throws InterruptedException {
		long diff = time - System.currentTimeMillis();
		if (diff <= 0) {
			return;
		}
		Thread.sleep(diff);
	}

	/**
	 * @param t the thread which must be interrupted and joined with a default timeout
	 * @return {@code true} for OK, {@code false} in case of an error.
	 */
	public static boolean interruptAndJoin(Thread t) {
		return interruptAndJoin(t, 0);
	}

	/**
	 * @param t the thread which must be interrupted
	 * @param waitTime a specific timeout for the join operation. A negative or zero value means to no timeout is
	 *            implied.
	 * @return {@code true} for OK, {@code false} in case of an error.
	 */
	public static boolean interruptAndJoin(Thread t, long waitTime) {
		if (t == null)
			return true;
		if (!t.isAlive())
			return true;

		t.interrupt();
		try {
			if (waitTime <= 0) {
				t.join();
			} else {
				t.join(waitTime);
			}
		} catch (InterruptedException e) {
			LOG.warn("interrupted while waiting for a thread to finish: " + e.getMessage(), e);
		}
		if (t.isAlive()) {
			LOG.warn("thread is still alive: " + t.getName());
			return false;
		}
		return true;
	}

	/**
	 * Wrapper for a runnable. The wrapper will delegate calls to Runnable.run() to the wrapped object. If the wrapped
	 * run() method terminates unexpectedly the run method will be called again.
	 */
	public static final class ForeverRunner implements Runnable {

		private final Runnable runnable;

		public ForeverRunner(Runnable runnable) {
			Validation.notNull(runnable);
			this.runnable = runnable;
		}

		@Override
		public void run() {
			while (true) {
				try {
					runnable.run();
					// regular shutdown
					return;
				} catch (Exception e) {
					LOG.warn("runnable crashed, restarting it. thread: " + Thread.currentThread().getName(), e);
				} catch (Throwable e) {
					LOG.error("runnable crashed with an error, will not restart. thread: " + Thread.currentThread().getName(), e);
				}
			}
		}
	}
}
