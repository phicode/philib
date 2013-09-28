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
package ch.bind.philib.util;

import ch.bind.philib.math.Calc;
import ch.bind.philib.validation.Validation;

/**
 * An implementation of the leaky bucket pattern for throughput control.
 * 
 * @author Philipp Meinen
 */
public final class LeakyBucket {

	/** the maximum amount that can be taken before the bucket is "empty" */
	private final long capacity;

	/** the time between individual takes on en empty bucket */
	private final long takeIntervalNs;

	/** the last time a recalculation was performed */
	private long lastRecalcNs;

	/** the current content of the bucket */
	private long content;

	private LeakyBucket(long takeIntervalNs, long capacity) {
		this.takeIntervalNs = takeIntervalNs;
		this.capacity = capacity;
		this.content = capacity;
	}

	public static LeakyBucket withTakesPerSecond(double takesPerSecond, long capacity) {
		Validation.isTrue(takesPerSecond >= 0.000001, "takesPerSecond must be >= 0.000001");
		long takeIntervalNs = (long) Math.ceil(1000000000f / takesPerSecond);
		return withTakeIntervalNs(takeIntervalNs, capacity);
	}

	public static LeakyBucket withTakeIntervalMs(long takeIntervalMs, long capacity) {
		return withTakeIntervalNs(takeIntervalMs * 1000000L, capacity);
	}

	public static LeakyBucket withTakeIntervalUs(long takeIntervalUs, long capacity) {
		return withTakeIntervalNs(takeIntervalUs * 1000L, capacity);
	}

	public static LeakyBucket withTakeIntervalNs(long takeIntervalNs, long capacity) {
		Validation.isTrue(takeIntervalNs > 0, "takeIntervalNs must be > 0");
		Validation.isTrue(capacity >= 1, "capacity must be >= 1");
		return new LeakyBucket(takeIntervalNs, capacity);
	}

	public long getCapacity() {
		return capacity;
	}

	public void take(long amount) {
		take(amount, System.nanoTime());
	}

	public void take(long amount, long timeNs) {
		recalculate(timeNs);
		content = Math.max(0, content - amount);
	}

	public long canTake() {
		return canTake(System.nanoTime());
	}

	/**
	 * @param timeNs a relative timestamp as provided by {@code System.nanoTime()} and <b>not</b>
	 *            {@code System.currentTimeMillis()}.
	 * @return
	 */
	public long canTake(long timeNs) {
		recalculate(timeNs);
		return content;
	}

	public long nextTakeNs() {
		return nextTakeNs(System.nanoTime());
	}

	/**
	 * @param timeNs a relative timestamp as provided by {@code System.nanoTime()} and <b>not</b>
	 *            {@code System.currentTimeMillis()}.
	 * @return
	 */
	public long nextTakeNs(long timeNs) {
		recalculate(timeNs);
		if (content > 0) {
			// available immediately
			return 0;
		}
		long nextTakeNano = lastRecalcNs + takeIntervalNs;
		return nextTakeNano - timeNs;
	}

	public long nextTakeUs() {
		return Calc.ceilDiv(nextTakeNs(), 1000L);
	}

	public long nextTakeMs() {
		return Calc.ceilDiv(nextTakeNs(), 1000000L);
	}

	public void sleepUntilAvailable() throws InterruptedException {
		long nextTakeNano;
		while ((nextTakeNano = nextTakeNs()) > 0) {
			long sleepMs = nextTakeNano / 1000000L;
			int sleepNano = (int) (nextTakeNano % 1000000L);
			Thread.sleep(sleepMs, sleepNano);
		}
	}

	private void recalculate(final long timeNs) {
		if (timeNs < lastRecalcNs) {
			// it seems that someone adjusted his clock backwards
			lastRecalcNs = timeNs;
		} else {
			long diff = timeNs - lastRecalcNs;
			long newlyAvailable = diff / takeIntervalNs;
			if (newlyAvailable > 0) {
				// do not overflow
				long newContent = content + newlyAvailable;
				if (newContent > capacity) {
					content = capacity;
					lastRecalcNs = timeNs;
				} else {
					lastRecalcNs += (newlyAvailable * takeIntervalNs);
					content = newContent;
				}
			}
		}
	}
}
