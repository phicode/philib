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

public abstract class Calc {

	protected Calc() {
	}

	/**
	 * Divides <i>num</i> by <i>divisor</i>, rounding up if <i>num</i> is not
	 * evenly divisible by <i>divisor</i>.
	 * @param num a number > 0
	 * @param divisor a number > 0
	 * @return
	 */
	public static long ceilDiv(long num, long divisor) {
		assert (num > 0 && divisor > 0);
		return ((num - 1) / divisor) + 1;
	}

	/**
	 * Divides <i>num</i> by <i>divisor</i>, rounding up if <i>num</i> is not
	 * evenly divisible by <i>divisor</i>.
	 * @param num a number > 0
	 * @param divisor a number > 0
	 * @return
	 */
	public static int ceilDiv(int num, int divisor) {
		assert (num > 0 && divisor > 0);
		return ((num - 1) / divisor) + 1;
	}

	public static long unsignedAdd(long a, long b) {
		long r = a + b;
		return addUnderOrOverflow(r, a, b) ? Long.MAX_VALUE : r;
	}

	/**
	 * Detects under or overflow after calculating <b>result = a + b</b>.
	 * 
	 * Derivation
	 * 
	 * <pre>
	 * sign bits:
	 * A B R  Outcome     A^R    B^R   ((A^R) & (B^R))
	 * 0 0 0  OK           0      0           0 
	 * 0 0 1  Overflow     1      1           1
	 * 0 1 0  OK           0      1           0
	 * 0 1 1  OK           1      0           0
	 * 1 0 0  OK           1      0           0
	 * 1 0 1  OK           0      1           0
	 * 1 1 0  Underflow    1      1           1
	 * 1 1 1  OK           0      0           0
	 * 
	 * So ((A^R) & (B^R)) produces a negative number (sign bit set) only if an under/overflow occurred.
	 * </pre>
	 * @param r
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean addUnderOrOverflow(long r, long a, long b) {
		return ((a ^ r) & (b ^ r)) < 0;
	}
}
