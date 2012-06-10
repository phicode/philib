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

import ch.bind.philib.validation.Validation;

public final class LeakyBucket {

	private final long capacity;

	private final long releaseIntervalNs;

	private long lastReleaseNs;

	private long numLeases;

	private LeakyBucket(long releaseIntervalNs, long capacity) {
		this.releaseIntervalNs = releaseIntervalNs;
		this.capacity = capacity;
	}

	/**
	 * @param releasePerSecond
	 * @param capacity
	 * @return
	 */
	public static LeakyBucket withReleasePerSecond(double releasePerSecond, long capacity) {
		Validation.isTrue(releasePerSecond >= 0.000001, "releasePerSecond must be >= 0.000001");
		Validation.isTrue(capacity >= 1, "capacity must be >= 1");
		long releaseIntervalNano = (long) Math.ceil(1000000000f / releasePerSecond);
		return new LeakyBucket(releaseIntervalNano, capacity);
	}

	public static LeakyBucket withReleaseIntervalNano(long releaseIntervalNano, long capacity) {
		Validation.isTrue(releaseIntervalNano > 0, "releaseIntervalNano must be > 0");
		Validation.isTrue(capacity >= 1, "capacity must be >= 1");
		return new LeakyBucket(releaseIntervalNano, capacity);
	}

	public long getCapacity() {
		return capacity;
	}

	public void acquire(long amount) {
		acquire(amount, System.nanoTime());
	}

	public void acquire(long amount, long timeNs) {
		recalc(timeNs);
		numLeases -= amount;
	}

	public long available() {
		return available(System.nanoTime());
	}

	public long available(long timeNs) {
		recalc(timeNs);
		return numLeases;
	}

	public long nextAvailableNano() {
		return nextAvailableNano(System.nanoTime());
	}

	public long nextAvailableNano(long timeNs) {
		recalc(timeNs);
		if (numLeases > 0) {
			// available immediately
			return 0;
		}
		else {
			long nextAvailNano = lastReleaseNs + releaseIntervalNs;
			return nextAvailNano - timeNs;
		}
	}

	public void sleepWhileNoneAvailable() throws InterruptedException {
		long nextAvailNano = nextAvailableNano();
		while (nextAvailNano > 0) {
			long sleepMs = nextAvailNano / 1000000L;
			int sleepNano = (int) (nextAvailNano % 1000000L);
			Thread.sleep(sleepMs, sleepNano);
			nextAvailNano = nextAvailableNano();
		}
	}

	private void recalc(final long timeNs) {
		if (timeNs < lastReleaseNs) {
			// it seems that someone adjusted his clock backwards
			lastReleaseNs = timeNs;
		}
		else {
			long elapsedNs = timeNs - lastReleaseNs;
			long numRelease = elapsedNs / releaseIntervalNs;
			long newLeases = numLeases + numRelease;

			// dont go over the limit
			if (newLeases > capacity) {
				numLeases = capacity;
				lastReleaseNs = timeNs;
			}
			else {
				numLeases = newLeases;
				lastReleaseNs += (numRelease * releaseIntervalNs);
			}
		}
	}
}
