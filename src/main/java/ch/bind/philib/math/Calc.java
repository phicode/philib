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
		return isAddUnderOrOverflow(a, b, r) ? Long.MAX_VALUE : r;
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
	 * @param a
	 * @param b
	 * @param r
	 * @return
	 */
	public static boolean isAddUnderOrOverflow(long a, long b, long r) {
		return ((a ^ r) & (b ^ r)) < 0;
	}

	/**
	 * Calculate the sum of all values from 1 to <code>end</code>, including.
	 * That is: <code>sum = 1 + 2 + 3 + ... + (end-1) + end</code> <br/>
	 * Examples:<br/>
	 * <code>
	 * f(0) = 0<br/>
	 * f(1) = 1<br/>
	 * f(2) = 3<br/>
	 * f(3) = 6<br/>
	 * f(10) = 55<br/>
	 * f(100) = 5050<br/>
	 * </code>
	 * 
	 * @param end The end value of the sum-range.
	 * @return The sum of all values from 1 to <code>end</code>, including.
	 */
	public static long sumOfRange(long end) {
		if (end < 1)
			return 0;
		// XXX: (end/2) * (end+1) would be possible as well, but for end=1 the
		// term end/2 would result in 0 and therefore be wrong.
		return (end * (end + 1)) / 2;
	}

	/**
	 * Calculates the sum of all values from <code>start</code> to
	 * <code>end</code>, including.
	 * 
	 * @param start The start value of the sum-range.
	 * @param end The end value of the sum-range.
	 * @return The sum of all values from <code>start</code> to <code>end</code>
	 *         , including.
	 */

	public static long sumOfRange(long start, long end) {
		if (start > end)
			return 0;
		return sumOfRange(end) - sumOfRange(start - 1);
	}

	// TODO
	// public static long sumOfRange(long start, long end, long increment) {

	public static int clip(int value, int min, int max) {
		return value < min ? min : (value > max ? max : value);
	}

	public static long clip(long value, long min, long max) {
		return value < min ? min : (value > max ? max : value);
	}

	public static double clip(double value, double min, double max) {
		return value < min ? min : (value > max ? max : value);
	}
}
