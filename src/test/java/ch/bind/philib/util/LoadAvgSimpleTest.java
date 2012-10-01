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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class LoadAvgSimpleTest {

	@Test
	public void withMillis() {
		LoadAvg la = LoadAvgSimple.forMillis(500);
		verify(la, 0, 0.01);

		// 0 load
		simulateLoadMs(la, 0, 100, 1000);
		System.out.println("after   0% load: " + la.getLoadAvgAsFactor());
		verify(la, 0, 0);

		// approx 25% load
		simulateLoadMs(la, 25, 75, 1000);
		System.out.println("after  25% load: " + la.getLoadAvgAsFactor());
		verify(la, 0.2, 0.3);

		//approx 50% load
		simulateLoadMs(la, 50, 50, 1000);
		System.out.println("after  50% load: " + la.getLoadAvgAsFactor());
		verify(la, 0.4, 0.6);

		//approx 75% load
		simulateLoadMs(la, 75, 25, 1000);
		System.out.println("after  75% load: " + la.getLoadAvgAsFactor());
		verify(la, 0.6, 0.8);

		// full load
		simulateLoadMs(la, 100, 0, 1000);
		System.out.println("after 100% load: " + la.getLoadAvgAsFactor());
		verify(la, 0.8, 1);

		simulateLoadMs(la, 75, 25, 1000);
		System.out.println("after  75% load: " + la.getLoadAvgAsFactor());
		verify(la, 0.6, 0.8);

		simulateLoadMs(la, 50, 50, 1000);
		System.out.println("after  50% load: " + la.getLoadAvgAsFactor());
		verify(la, 0.4, 0.6);

		simulateLoadMs(la, 25, 75, 1000);
		System.out.println("after  25% load: " + la.getLoadAvgAsFactor());
		verify(la, 0.2, 0.4);

		simulateLoadMs(la, 0, 100, 1000);
		System.out.println("after   0% load: " + la.getLoadAvgAsFactor());
		verify(la, 0, 0.1);
	}

	private void verify(LoadAvg la, double min, double max) {
		double factor = la.getLoadAvgAsFactor();
		assertTrue(factor >= min, factor + " should be >= " + min);
		assertTrue(factor <= max, factor + " should be <= " + max);
	}

	private void simulateLoadMs(LoadAvg la, int work, int idle, int durationMs) {
		long durationNs = durationMs * 1000000L;
		long workNs = work * 1000000L;
		long idleNs = idle * 1000000L;
		long now = System.nanoTime();
		long end = now + durationNs;
		boolean atWork = true;
		long switchWorkIdleAt = now + workNs;

		while (now < end) {
			now = System.nanoTime();
			if (now > switchWorkIdleAt) {
				if (atWork) {
					la.logWorkNs(workNs);
					switchWorkIdleAt = now + idleNs;
				} else {
					switchWorkIdleAt = now + workNs;
				}
				atWork = !atWork;
			}
		}
	}
}
