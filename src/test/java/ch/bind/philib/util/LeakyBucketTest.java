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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LeakyBucketTest {

	private static final long SEC = 1000000000L;

	@Test
	public void normalTimeInitialCalc() {
		LeakyBucket bc = LeakyBucket.withReleasePerSecond(2500, 1000);
		assertEquals(1000, bc.available());
		assertEquals(1000, bc.getCapacity());
	}

	@Test
	public void fakeTimeInitialCalc() {
		LeakyBucket bc = LeakyBucket.withReleasePerSecond(100, 1000);
		// not quite a second
		long time = SEC - 1;
		assertEquals(99, bc.available(time));
		time++; // 1sec
		assertEquals(100, bc.available(time));
	}

	@Test
	public void fakeTimeCountSmallSteps() {
		LeakyBucket bc = LeakyBucket.withReleasePerSecond(2500, 2500);
		long time = SEC;
		long interval = 400000; // SEC / 2500
		assertEquals(2500, bc.available(time));
		assertEquals(0, bc.nextAvailableNano(time));
		// simulate one whole day
		for (int numSeconds = 0; numSeconds < 86400; numSeconds++) {
			assertEquals(2500, bc.available(time));
			assertEquals(0, bc.nextAvailableNano(time));

			bc.acquire(2500, time);
			assertEquals(0, bc.available(time));
			assertEquals(interval, bc.nextAvailableNano(time));
			time++; // x sec + 1 nano
			assertEquals(0, bc.available(time));
			assertEquals(interval - 1, bc.nextAvailableNano(time));

			// x.5 sec - 1 nano
			time += (SEC / 2 - 2);
			assertEquals(1249, bc.available(time));
			// x.5 sec
			time++;
			assertEquals(1250, bc.available(time));

			// x.5 sec + 1 nano
			time++;
			assertEquals(1250, bc.available(time));

			// x + 1 sec - 1 nano
			time += (SEC / 2 - 2);
			assertEquals(2499, bc.available(time));

			// x + 1sec
			time++;
			assertEquals(2500, bc.available(time));
		}
	}

	@Test
	public void acquireWithRealTime() {
		LeakyBucket bc = LeakyBucket.withReleasePerSecond(2500, 1000);
		long start = System.nanoTime();
		assertEquals(1000, bc.available());
		bc.acquire(1000);
		long moreAcquired = 0;
		while (moreAcquired < 5000) {
			long time = System.nanoTime();
			long nextAvail = bc.nextAvailableNano(time);
			long a = bc.available(time);
			if (a > 0) {
				assertEquals(nextAvail, 0); // available now
				bc.acquire(a, time);
				moreAcquired += a;
			} else {
				assertTrue(nextAvail > 0);
			}
		}
		long end = System.nanoTime();
		long totalTime = end - start;
		// 2 milliseconds or 0.1% should be ok even for lame computers
		long delta = 5 * 1000 * 1000;
		// should take 2 seconds
		long min = 2 * 1000 * 1000 * 1000 - delta;
		long max = 2 * 1000 * 1000 * 1000 + delta;
		assertTrue(totalTime >= min && totalTime <= max);
		System.out.println("time: " + totalTime);
	}

	@Test
	public void exactRelease() {
		long intervalNs = 100 * 1000000L; // 100ms
		long i3 = intervalNs * 3;
		long i4 = intervalNs * 4;
		LeakyBucket bc = LeakyBucket.withReleaseIntervalNano(intervalNs, 1);
		for (long t = 0; t < intervalNs; t += 10) {
			assertTrue(bc.available(t) == 0);
			assertTrue(bc.nextAvailableNano(t) == intervalNs - t);
		}
		for (long t = intervalNs; t < i3; t += 10) {
			assertTrue(bc.available(t) == 1);
			assertTrue(bc.nextAvailableNano(t) == 0);
		}
		bc.acquire(1, i3);
		for (long t = i3; t < i4; t += 10) {
			assertTrue(bc.available(t) == 0);
			assertTrue(bc.nextAvailableNano(t) == i4 - t);
		}
		assertTrue(bc.available(i4 + 1) == 1);
		assertTrue(bc.nextAvailableNano(i4 + 1) == 0);
	}
}
