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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class LoadAvgTest {

	private static final Logger LOG = LoggerFactory.getLogger(LoadAvgTest.class);

	@Test
	public void noop() {
		assertEquals(LoadAvgNoop.INSTANCE.getLoadAvg(), 0);
		LoadAvgNoop.INSTANCE.logWorkNs(999999999);
		assertEquals(LoadAvgNoop.INSTANCE.getLoadAvg(), 0);
		LoadAvgNoop.INSTANCE.logWorkMs(999999999);
		assertEquals(LoadAvgNoop.INSTANCE.getLoadAvg(), 0);

		double z = 0;
		assertEquals(LoadAvgNoop.INSTANCE.asFactor(0), z);
		assertEquals(LoadAvgNoop.INSTANCE.asFactor(1000), z);
		assertEquals(LoadAvgNoop.INSTANCE.asFactor(1000000), z);
	}

	@Test
	public void withSeconds() {
		LoadAvgSimple las = LoadAvgSimple.forSeconds(1);
		runTest(las);
	}

	@Test
	public void withMillis() {
		LoadAvgSimple las = LoadAvgSimple.forMillis(1000);
		runTest(las);
	}

	@Test
	public void withMicros() {
		LoadAvgSimple las = LoadAvgSimple.forMicros(1000 * 1000);
		runTest(las);
	}

	@Test
	public void withNanos() {
		LoadAvgSimple las = LoadAvgSimple.forNanos(1000 * 1000 * 1000);
		runTest(las);
	}

	// assumes a 1 second millis load-avg object
	private static void runTest(final LoadAvgSimple las) {
		// 0 load
		double load = simulateLoadMs(las, 0, 0, 5000);
		verify(load, 0, 0);

		// approx 25% load
		load = simulateLoadMs(las, 25, 1, 5000);
		verify(load, 0.2, 0.3);

		// approx 50% load
		load = simulateLoadMs(las, 50, 2, 5000);
		verify(load, 0.45, 0.55);

		// approx 75% load
		load = simulateLoadMs(las, 75, 3, 5000);
		verify(load, 0.7, 0.8);

		// full load
		load = simulateLoadMs(las, 100, 4, 5000);
		verify(load, 0.95, 1);

		load = simulateLoadMs(las, 75, 5, 5000);
		verify(load, 0.7, 0.8);

		load = simulateLoadMs(las, 50, 6, 5000);
		verify(load, 0.45, 0.55);

		load = simulateLoadMs(las, 25, 7, 5000);
		verify(load, 0.2, 0.3);

		load = simulateLoadMs(las, 0, 8, 5000);
		verify(load, 0, 0.05);
	}

	private static void verify(double load, double min, double max) {
		assertTrue(load >= min, load + " should be >= " + min);
		assertTrue(load <= max, load + " should be <= " + max);
	}

	private static double simulateLoadMs(LoadAvgSimple las, int work, int timeslot, int durationMs) {
		final long nsPerMs = 1000000L;
		final long logWorkEveryMs = work * nsPerMs / 100;

		long simulatedTimeNs = 123456789L + (timeslot * durationMs * nsPerMs);
		for (int i = 0; i < durationMs; i++) {
			las.logWorkNs(simulatedTimeNs, logWorkEveryMs);
			simulatedTimeNs += nsPerMs;
		}
		long loadAvg = las.logWorkNs(simulatedTimeNs, 0);
		double load = las.asFactor(loadAvg);
		LOG.debug(String.format("after %3d%% load: %f\n", work, load));
		return load;
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ctorSec() {
		LoadAvgSimple.forSeconds(0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ctorMsec() {
		LoadAvgSimple.forMillis(0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ctorUsec() {
		LoadAvgSimple.forMicros(0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ctorNsec() {
		LoadAvgSimple.forNanos(0);
	}
}
