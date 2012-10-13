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

	private static final boolean debug = false;

	@Test
	public void withMillis() {
		LoadAvg la = LoadAvgSimple.forMillis(250);
		verify(la, 0, 0.01);

		// 0 load
		simulateLoadMs(la, 0, 1000);
		verify(la, 0, 0);

		// approx 25% load
		simulateLoadMs(la, 25, 1000);
		verify(la, 0.2, 0.3);

		// approx 50% load
		simulateLoadMs(la, 50, 1000);
		verify(la, 0.45, 0.55);

		// approx 75% load
		simulateLoadMs(la, 75, 1000);
		verify(la, 0.7, 0.8);

		// full load
		simulateLoadMs(la, 100, 1000);
		verify(la, 0.95, 1);

		simulateLoadMs(la, 75, 1000);
		verify(la, 0.7, 0.8);

		simulateLoadMs(la, 50, 1000);
		verify(la, 0.45, 0.55);

		simulateLoadMs(la, 25, 1000);
		verify(la, 0.2, 0.3);

		simulateLoadMs(la, 0, 1000);
		verify(la, 0, 0.05);
	}

	private static void verify(LoadAvg la, double min, double max) {
		double factor = la.getLoadAvgAsFactor();
		assertTrue(factor >= min, factor + " should be >= " + min);
		assertTrue(factor <= max, factor + " should be <= " + max);
	}

	private static void simulateLoadMs(LoadAvg la, int work, int durationMs) {
		final long nsPerMs = 1000000L;
		final long startNs = System.nanoTime();
		final long logWorkEveryMs = work * nsPerMs / 100;

		for (int i = 0; i < durationMs; i++) {
			final long endOfLoop = startNs + (nsPerMs * (i + 1));
			while (System.nanoTime() < endOfLoop) {
				// busy loop for 1 ms
			}
			la.logWorkNs(logWorkEveryMs);
		}
		if (debug) {
			System.out.printf("after %3d%% load: %f\n", work, la.getLoadAvgAsFactor());
		}
	}
}
