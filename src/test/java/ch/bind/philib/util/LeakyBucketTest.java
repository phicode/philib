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

import ch.bind.philib.TestUtil;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LeakyBucketTest {

	private static final long NS_PER_SEC = 1000000000L;

	@Test(timeOut = 2000)
	public void normalTimeInitialCalc() {
		LeakyBucket lb = LeakyBucket.withTakesPerSecond(2500, 1000);
		assertEquals(1000, lb.canTake());
		assertEquals(1000, lb.getCapacity());
	}

	@Test(timeOut = 2000)
	public void fakeTimeInitialCalc() {
		LeakyBucket lb = LeakyBucket.withTakesPerSecond(100, 1000);

		// take everything
		lb.take(1000, 0);

		// not quite a second later
		long timeNs = NS_PER_SEC - 1;
		assertEquals(99, lb.canTake(timeNs));

		timeNs = NS_PER_SEC;
		assertEquals(100, lb.canTake(timeNs));
	}

	@Test(timeOut = 20000)
	public void fakeTimeCountSmallSteps() {
		LeakyBucket lb = LeakyBucket.withTakesPerSecond(2500, 2500);
		long timeNs = NS_PER_SEC;
		long interval = 400000; // SEC / 2500
		assertEquals(2500, lb.canTake(timeNs));
		assertEquals(0, lb.nextTakeNs(timeNs));
		// simulate one whole day
		for (int numSeconds = 0; numSeconds < 86400; numSeconds++) {
			assertEquals(2500, lb.canTake(timeNs));
			assertEquals(0, lb.nextTakeNs(timeNs));

			lb.take(2500, timeNs);
			assertEquals(0, lb.canTake(timeNs));
			assertEquals(interval, lb.nextTakeNs(timeNs));
			timeNs++; // x sec + 1 nano
			assertEquals(0, lb.canTake(timeNs));
			assertEquals(interval - 1, lb.nextTakeNs(timeNs));

			// x.5 sec - 1 nano
			timeNs += (NS_PER_SEC / 2 - 2);
			assertEquals(1249, lb.canTake(timeNs));
			// x.5 sec
			timeNs++;
			assertEquals(1250, lb.canTake(timeNs));

			// x.5 sec + 1 nano
			timeNs++;
			assertEquals(1250, lb.canTake(timeNs));

			// x + 1 sec - 1 nano
			timeNs += (NS_PER_SEC / 2 - 2);
			assertEquals(2499, lb.canTake(timeNs));

			// x + 1sec
			timeNs++;
			assertEquals(2500, lb.canTake(timeNs));
		}
	}

	@Test(timeOut = 10000)
	public void takeWithRealTime() {
		TestUtil.gcAndSleep(50);
		LeakyBucket lb = LeakyBucket.withTakesPerSecond(25000, 1000);
		assertEquals(lb.canTake(), 1000);
		long start = System.nanoTime();
		lb.take(1000);
		assertEquals(lb.canTake(start), 0);
		long moreAcquired = 0;
		while (moreAcquired < 5000) {
			long time = System.nanoTime();
			long nextAvail = lb.nextTakeNs(time);
			long a = lb.canTake(time);
			if (a > 0) {
				assertEquals(nextAvail, 0); // available now
				lb.take(a, time);
				moreAcquired += a;
			} else {
				assertTrue(nextAvail > 0);
			}
		}
		long end = System.nanoTime();
		long totalTime = end - start;
		// 2 milliseconds or 0.1% should be ok even for lame computers
		long delta = 5 * 1000 * 1000;
		// should take 0.2 seconds
		long min = 200 * 1000 * 1000 - delta;
		long max = 200 * 1000 * 1000 + delta;
		assertTrue(totalTime >= min && totalTime <= max, "total: " + totalTime);
	}

	@Test(timeOut = 5000)
	public void exactRelease() {
		long intervalMs = 100; // 10 per sec
		long intervalNs = intervalMs * 1000000L;
		long i3 = intervalNs * 3;
		long i4 = intervalNs * 4;

		LeakyBucket lb = LeakyBucket.withTakeIntervalMs(intervalMs, 1);
		long t = 0;
		lb.take(1, t);
		while (t < intervalNs) {
			assertEquals(lb.canTake(t), 0);
			assertEquals(lb.nextTakeNs(t), intervalNs - t);
			t += 1000;
		}
		while (t < i3) {
			assertEquals(lb.canTake(t), 1);
			assertEquals(lb.nextTakeNs(t), 0);
			t += 1000;
		}

		lb.take(1, i3);
		while (t < i4) {
			assertEquals(lb.canTake(t), 0);
			assertEquals(lb.nextTakeNs(t), i4 - t);
			t += 1000;
		}

		assertEquals(lb.canTake(i4 + 1), 1);
		assertEquals(lb.nextTakeNs(i4 + 1), 0);
	}
}
