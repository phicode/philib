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

import org.testng.annotations.Test;

import ch.bind.philib.util.Counter;

public class CounterTest {

	private static final String EXPECT_ZERO = "a[unit=ms, #adds=0, total=0, min=N/A, max=N/A, avg=N/A]";

	@Test
	public void zero() {
		Counter pm = new Counter("a", "ms");
		assertEquals(pm.toString(), EXPECT_ZERO);
	}

	@Test
	public void one() {
		Counter pm = new Counter("a", "ms");
		pm.add(500);
		assertEquals(pm.toString(), "a[unit=ms, #adds=1, total=500, min=500, max=500, avg=500.000]");
	}

	@Test
	public void two() {
		Counter pm = new Counter("a", "ms");
		pm.add(100);
		pm.add(500);
		assertEquals(pm.toString(), "a[unit=ms, #adds=2, total=600, min=100, max=500, avg=300.000]");
	}

	@Test
	public void three() {
		Counter pm = new Counter("a", "ms");
		pm.add(200);
		pm.add(100);
		pm.add(300);
		assertEquals(pm.toString(), "a[unit=ms, #adds=3, total=600, min=100, max=300, avg=200.000]");
	}

	@Test
	public void many() {
		Counter pm = new Counter("a", "ms");
		for (int i = 1000; i <= 10000; i++) {
			pm.add(i);
		}
		for (int i = 999; i >= -100; i--) {
			pm.add(i);
		}
		assertEquals(pm.toString(), "a[unit=ms, #adds=10000, total=50005000, min=1, max=10000, avg=5000.500]");
	}

	@Test
	public void reset() {
		Counter pm = new Counter("a", "ms");
		assertEquals(pm.toString(), EXPECT_ZERO);
		pm.add(100);
		assertEquals(pm.toString(), "a[unit=ms, #adds=1, total=100, min=100, max=100, avg=100.000]");
		pm.reset();
		assertEquals(pm.toString(), EXPECT_ZERO);
	}
}
