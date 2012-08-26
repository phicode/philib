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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class PhiMathTest {

	@Test
	public void sumOfRange1() {
		assertEquals(0, PhiMath.sumOfRange(-9999));
		assertEquals(0, PhiMath.sumOfRange(-1));
		assertEquals(1, PhiMath.sumOfRange(1));
		assertEquals(3, PhiMath.sumOfRange(2));
		assertEquals(6, PhiMath.sumOfRange(3));
		assertEquals(5050, PhiMath.sumOfRange(100));
	}

	@Test
	public void sumOfRange2() {
		assertEquals(0, PhiMath.sumOfRange(10, 9));
		assertEquals(0, PhiMath.sumOfRange(1, 0));
		assertEquals(0, PhiMath.sumOfRange(-9999, -9998));
		assertEquals(0, PhiMath.sumOfRange(-1, 0));
		assertEquals(1, PhiMath.sumOfRange(1, 1));
		assertEquals(3, PhiMath.sumOfRange(1, 2));
		assertEquals(2, PhiMath.sumOfRange(2, 2));
		assertEquals(6, PhiMath.sumOfRange(1, 3));
		assertEquals(5, PhiMath.sumOfRange(2, 3));
		assertEquals(3, PhiMath.sumOfRange(3, 3));
		assertEquals(5050, PhiMath.sumOfRange(1, 100));
		assertEquals(5049, PhiMath.sumOfRange(2, 100));
		assertEquals(5047, PhiMath.sumOfRange(3, 100));
		assertEquals(5044, PhiMath.sumOfRange(4, 100));
		assertEquals(5040, PhiMath.sumOfRange(5, 100));
	}
}
