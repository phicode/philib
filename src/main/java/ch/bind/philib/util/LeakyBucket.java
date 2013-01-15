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

	/** the maximum amount before the bucket will drop */
	private final long capacity;

	/** how often the bucket will leak until it is empty */
	private final long leakIntervalNs;

	/** the last time the bucket leaked */
	private long lastLeakNs;

	/** the current content of the bucket */
	private long current;

	private LeakyBucket(long leakIntervalNs, long capacity) {
		this.leakIntervalNs = leakIntervalNs;
		this.capacity = capacity;
	}

	public static LeakyBucket withLeakPerSecond(double leakPerSecond, long capacity) {
		Validation.isTrue(leakPerSecond >= 0.000001, "leakPerSecond must be >= 0.000001");
		long leakIntervalNs = (long) Math.ceil(1000000000f / leakPerSecond);
		return withLeakIntervalNs(leakIntervalNs, capacity);
	}

	public static LeakyBucket withLeakIntervalMs(long leakIntervalMs, long capacity) {
		return withLeakIntervalNs(leakIntervalMs * 1000000L, capacity);
	}

	public static LeakyBucket withLeakIntervalUs(long leakIntervalUs, long capacity) {
		return withLeakIntervalNs(leakIntervalUs * 1000L, capacity);
	}

	public static LeakyBucket withLeakIntervalNs(long leakIntervalNs, long capacity) {
		Validation.isTrue(leakIntervalNs > 0, "leakIntervalNs must be > 0");
		Validation.isTrue(capacity >= 1, "capacity must be >= 1");
		return new LeakyBucket(leakIntervalNs, capacity);
	}

	public long getCapacity() {
		return capacity;
	}

	public void fill(long amount) {
		fill(amount, System.nanoTime());
	}

	public void fill(long amount, long timeNs) {
		leak(timeNs);
		current += amount;
	}

	public long canFill() {
		return canFill(System.nanoTime());
	}

	public long canFill(long timeNs) {
		leak(timeNs);
		return capacity - current;
	}

	public long nextFillNs() {
		return nextFillNs(System.nanoTime());
	}

	public long nextFillNs(long timeNs) {
		leak(timeNs);
		if (current < capacity) {
			// available immediately
			return 0;
		}
		long nextLeakNano = lastLeakNs + leakIntervalNs;
		return nextLeakNano - timeNs;
	}

	public long nextFillUs() {
		return Calc.ceilDiv(nextFillNs(), 1000L);
	}

	public long nextFillMs() {
		return Calc.ceilDiv(nextFillNs(), 1000000L);
	}

	public void sleepWhileNotFillable() throws InterruptedException {
		long nextFillNano;
		while ((nextFillNano = nextFillNs()) > 0) {
			long sleepMs = nextFillNano / 1000000L;
			int sleepNano = (int) (nextFillNano % 1000000L);
			Thread.sleep(sleepMs, sleepNano);
		}
	}

	private void leak(final long timeNs) {
		if (timeNs < lastLeakNs) {
			// it seems that someone adjusted his clock backwards
			lastLeakNs = timeNs;
		} else {
			long diff = timeNs - lastLeakNs;
			long canLeak = diff / leakIntervalNs;
			if (canLeak > 0) {
				// dont go below 0
				if (canLeak >= current) {
					lastLeakNs = (leakIntervalNs * current);
					current = 0;
				} else {
					lastLeakNs += (leakIntervalNs * canLeak);
					current -= canLeak;
				}
			}
			if (current == 0) {
				lastLeakNs = timeNs;
			}
		}
	}
}
