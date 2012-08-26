/*
 * Copyright (c) 2009-2011 Philipp Meinen <philipp@bind.ch>
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
package ch.bind.philib;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class IntervalsTest {

	@Test
	public void chooseInterval1() {
		for (int i = 0; i <= 10; i++) {
			assertEquals(1, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval2() {
		for (int i = 11; i <= 20; i++) {
			assertEquals(2, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval5() {
		for (int i = 21; i <= 50; i++) {
			assertEquals(5, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval10() {
		for (int i = 51; i <= 100; i++) {
			assertEquals(10, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval25() {
		for (int i = 101; i <= 250; i++) {
			assertEquals(25, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval50() {
		for (int i = 251; i <= 500; i++) {
			assertEquals(50, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval100() {
		for (int i = 501; i <= 1000; i++) {
			assertEquals(100, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval250() {
		for (int i = 1001; i <= 2500; i++) {
			assertEquals(250, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval500() {
		for (int i = 2501; i <= 5000; i++) {
			assertEquals(500, Intervals.chooseInterval(i, 10));
		}
		assertEquals(1000, Intervals.chooseInterval(5001, 10));
	}
}
