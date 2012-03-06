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

import ch.bind.philib.validation.SimpleValidation;

public final class BucketCounter {

	private final long capacity;

	private final long releaseIntevalNano;

	private long lastReleaseNano;

	private long currentCapacity;

	private BucketCounter(long capacity, long releaseIntevalNano) {
		this.capacity = capacity;
		this.releaseIntevalNano = releaseIntevalNano;
	}

	public static BucketCounter withReleasePerSecond(double releasePerSecond, long capacity) {
		SimpleValidation.isTrue(releasePerSecond >= 0.000001, "releasePerSecond must be >= 0.000001");
		SimpleValidation.isTrue(capacity >= 1, "capacity must be >= 1");
		long releaseIntervalNano = (long) Math.ceil(1000000000f / releasePerSecond);
		return new BucketCounter(capacity, releaseIntervalNano);
	}

	public long getCapacity() {
		return capacity;
	}

	public void acquire(long amount) {
		acquire(amount, System.nanoTime());
	}

	public void acquire(long amount, long timeNano) {
		recalc(timeNano);
		currentCapacity -= amount;
	}

	public long available() {
		return available(System.nanoTime());
	}

	public long available(long timeNano) {
		recalc(timeNano);
		return currentCapacity;
	}

	public long nextAvailableNano() {
		return nextAvailableNano(System.nanoTime());
	}

	public long nextAvailableNano(long timeNano) {
		recalc(timeNano);
		if (currentCapacity > 0) {
			// available immediately
			return 0;
		}
		else {
			long nextAvailNano = lastReleaseNano + releaseIntevalNano;
			return nextAvailNano - timeNano;
		}
	}

	private void recalc(long timeNano) {
		assert (timeNano >= lastReleaseNano);
		long elapsedNano = timeNano - lastReleaseNano;
		long numRelease = elapsedNano / releaseIntevalNano;
		long newVal = currentCapacity + numRelease;

		// dont go over the limit
		if (newVal > capacity) {
			currentCapacity = capacity;
			lastReleaseNano = timeNano;
		}
		else {
			currentCapacity = newVal;
			lastReleaseNano += (numRelease * releaseIntevalNano);
		}
	}
}
