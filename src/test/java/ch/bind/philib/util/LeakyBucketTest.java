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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class LeakyBucketTest {

	private static final long SEC = 1000000000L;

	@Test
	public void normalTimeInitialCalc() {
		LeakyBucket bc = LeakyBucket.withLeakPerSecond(2500, 1000);
		assertEquals(1000, bc.canFill());
		assertEquals(1000, bc.getCapacity());
	}

	@Test
	public void fakeTimeInitialCalc() {
		LeakyBucket bc = LeakyBucket.withLeakPerSecond(100, 1000);
		// fill the bucket
		bc.fill(1000, 0);
		// not quite a second a second later
		long time = SEC - 1;
		assertEquals(99, bc.canFill(time));
		time++; // 1sec
		assertEquals(100, bc.canFill(time));
	}

	@Test
	public void fakeTimeCountSmallSteps() {
		LeakyBucket bc = LeakyBucket.withLeakPerSecond(2500, 2500);
		long time = SEC;
		long interval = 400000; // SEC / 2500
		assertEquals(2500, bc.canFill(time));
		assertEquals(0, bc.nextFillNs(time));
		// simulate one whole day
		for (int numSeconds = 0; numSeconds < 86400; numSeconds++) {
			assertEquals(2500, bc.canFill(time));
			assertEquals(0, bc.nextFillNs(time));

			bc.fill(2500, time);
			assertEquals(0, bc.canFill(time));
			assertEquals(interval, bc.nextFillNs(time));
			time++; // x sec + 1 nano
			assertEquals(0, bc.canFill(time));
			assertEquals(interval - 1, bc.nextFillNs(time));

			// x.5 sec - 1 nano
			time += (SEC / 2 - 2);
			assertEquals(1249, bc.canFill(time));
			// x.5 sec
			time++;
			assertEquals(1250, bc.canFill(time));

			// x.5 sec + 1 nano
			time++;
			assertEquals(1250, bc.canFill(time));

			// x + 1 sec - 1 nano
			time += (SEC / 2 - 2);
			assertEquals(2499, bc.canFill(time));

			// x + 1sec
			time++;
			assertEquals(2500, bc.canFill(time));
		}
	}

	@Test
	public void fillWithRealTime() {
		LeakyBucket bc = LeakyBucket.withLeakPerSecond(2500, 1000);
		long start = System.nanoTime();
		assertEquals(bc.canFill(), 1000);
		bc.fill(1000);
		assertEquals(bc.canFill(), 0);
		long moreAcquired = 0;
		while (moreAcquired < 5000) {
			long time = System.nanoTime();
			long nextAvail = bc.nextFillNs(time);
			long a = bc.canFill(time);
			if (a > 0) {
				assertEquals(nextAvail, 0); // available now
				bc.fill(a, time);
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
		assertTrue(totalTime >= min && totalTime <= max, "total: " + totalTime);
	}

	@Test
	public void exactRelease() {
		long intervalMs = 100;
		long intervalNs = intervalMs * 1000000L;
		long i3 = intervalNs * 3;
		long i4 = intervalNs * 4;
		LeakyBucket bc = LeakyBucket.withLeakIntervalMs(intervalMs, 1);
		bc.fill(1);
		for (long t = 0; t < intervalNs; t += 10) {
			assertTrue(bc.canFill(t) == 0);
			assertTrue(bc.nextFillNs(t) == intervalNs - t);
		}
		for (long t = intervalNs; t < i3; t += 10) {
			assertTrue(bc.canFill(t) == 1);
			assertTrue(bc.nextFillNs(t) == 0);
		}
		bc.fill(1, i3);
		for (long t = i3; t < i4; t += 10) {
			assertTrue(bc.canFill(t) == 0);
			assertTrue(bc.nextFillNs(t) == i4 - t);
		}
		assertTrue(bc.canFill(i4 + 1) == 1);
		assertTrue(bc.nextFillNs(i4 + 1) == 0);
	}
}
