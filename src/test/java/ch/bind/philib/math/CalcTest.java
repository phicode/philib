/*
 * Copyright (c) 2006-2011 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.math;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CalcTest {

	@Test
	public void sumOfRange1() {
		assertEquals(0, Calc.sumOfRange(-9999));
		assertEquals(0, Calc.sumOfRange(-1));
		assertEquals(1, Calc.sumOfRange(1));
		assertEquals(3, Calc.sumOfRange(2));
		assertEquals(6, Calc.sumOfRange(3));
		assertEquals(5050, Calc.sumOfRange(100));
	}

	@Test
	public void sumOfRange2() {
		assertEquals(0, Calc.sumOfRange(10, 9));
		assertEquals(0, Calc.sumOfRange(1, 0));
		assertEquals(0, Calc.sumOfRange(-9999, -9998));
		assertEquals(0, Calc.sumOfRange(-1, 0));
		assertEquals(1, Calc.sumOfRange(1, 1));
		assertEquals(3, Calc.sumOfRange(1, 2));
		assertEquals(2, Calc.sumOfRange(2, 2));
		assertEquals(6, Calc.sumOfRange(1, 3));
		assertEquals(5, Calc.sumOfRange(2, 3));
		assertEquals(3, Calc.sumOfRange(3, 3));
		assertEquals(5050, Calc.sumOfRange(1, 100));
		assertEquals(5049, Calc.sumOfRange(2, 100));
		assertEquals(5047, Calc.sumOfRange(3, 100));
		assertEquals(5044, Calc.sumOfRange(4, 100));
		assertEquals(5040, Calc.sumOfRange(5, 100));
	}

	@Test
	public void ceilDivInt() {
		assertEquals(Calc.ceilDiv(0, 1), 0);
		assertEquals(Calc.ceilDiv(0, Integer.MAX_VALUE), 0);

		assertEquals(Calc.ceilDiv(3, 1), 3);
		assertEquals(Calc.ceilDiv(3, 2), 2);
		assertEquals(Calc.ceilDiv(3, 3), 1);
		assertEquals(Calc.ceilDiv(3, 4), 1);
		assertEquals(Calc.ceilDiv(3, 5), 1);

		for (int d = 1; d < 1000; d++) {
			assertEquals(Calc.ceilDiv(1, d), 1);
		}
	}

	@Test
	public void ceilDivLong() {
		assertEquals(Calc.ceilDiv(0L, 1), 0);
		assertEquals(Calc.ceilDiv(0L, Long.MAX_VALUE), 0);

		assertEquals(Calc.ceilDiv(3L, 1), 3);
		assertEquals(Calc.ceilDiv(3L, 2), 2);
		assertEquals(Calc.ceilDiv(3L, 3), 1);
		assertEquals(Calc.ceilDiv(3L, 4), 1);
		assertEquals(Calc.ceilDiv(3L, 5), 1);

		for (long d = 1; d < 1000; d++) {
			assertEquals(Calc.ceilDiv(1L, d), 1);
		}
	}

	@Test
	public void unsignedAdd() {
		// + add + with no overflow
		assertEquals(Calc.unsignedAdd(Long.MAX_VALUE - 5, 4), Long.MAX_VALUE - 1);

		// + add + with overflow
		assertEquals(Calc.unsignedAdd(Long.MAX_VALUE - 5, 6), Long.MAX_VALUE);
	}

	@Test
	public void isAddUnderOverflow() {
		assertFalse(Calc.isAddUnderOrOverflow(5, 5, 5));
		assertTrue(Calc.isAddUnderOrOverflow(5, 5, -5));
		assertFalse(Calc.isAddUnderOrOverflow(5, -5, 5));
		assertFalse(Calc.isAddUnderOrOverflow(5, -5, -5));
		assertFalse(Calc.isAddUnderOrOverflow(-5, 5, 5));
		assertFalse(Calc.isAddUnderOrOverflow(-5, 5, -5));
		assertTrue(Calc.isAddUnderOrOverflow(-5, -5, 5));
		assertFalse(Calc.isAddUnderOrOverflow(-5, -5, -5));
	}

	@Test
	public void clipInt() {
		for (int i = 0; i < 100; i++) {
			assertEquals(Calc.clip(i, 100, 200), 100);
		}
		for (int i = 100; i < 200; i++) {
			assertEquals(Calc.clip(i, 100, 200), i);
		}
		for (int i = 200; i < 300; i++) {
			assertEquals(Calc.clip(i, 100, 200), 200);
		}
	}

	@Test
	public void clipLong() {
		for (long i = 0; i < 100; i++) {
			assertEquals(Calc.clip(i, 100, 200), 100L);
		}
		for (long i = 100; i < 200; i++) {
			assertEquals(Calc.clip(i, 100, 200), i);
		}
		for (long i = 200; i < 300; i++) {
			assertEquals(Calc.clip(i, 100, 200), 200L);
		}
	}

	@Test
	public void clipDouble() {
		for (double i = 0; i < 100; i++) {
			assertEquals(Calc.clip(i, 100, 200), 100f, 0.001);
		}
		for (double i = 100; i < 200; i++) {
			assertEquals(Calc.clip(i, 100, 200), i, 0.001);
		}
		for (double i = 200; i < 300; i++) {
			assertEquals(Calc.clip(i, 100, 200), 200f, 0.001);
		}
	}
}
