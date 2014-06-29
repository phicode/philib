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

package ch.bind.philib.lang;

/**
 * Provides helper methods for object comparison.
 *
 * @author Philipp Meinen
 */
public abstract class CompareUtil {

	protected CompareUtil() {
	}

	/**
	 * A null-safe equality helper.
	 *
	 * <pre>
	 * input                   return value
	 * ------------------------------------
	 * a == b               => true
	 * a == null, b != null => false
	 * a != null, b == null => false
	 * otherwise            => a.equals(b)
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static boolean equals(final Object a, final Object b) {
		if (a == b)
			return true;
		if (a == null) {
			// b is not null -> not equal
			return false;
		}
		// this check could be simplified by only invoking a.equals(b) and
		// leaving it up to the specific equals implementation to check for
		// nulls. on the other hand, here we are with the power to find out
		// right here and now
		return b != null && a.equals(b);
	}

	/**
	 * A null-safe compare helper.
	 *
	 * <pre>
	 * input                   return value
	 * ------------------------------------
	 * a == b                =>  0
	 * a == null, b != null  => -1
	 * a != null, b == null  =>  1
	 * otherwise             =>  a.compareTo(b)
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static <T> int compare(final Comparable<T> a, final T b) {
		if (a == b)
			return 0;
		if (a == null) {
			// b is not null
			return -1; // a < b
		}
		if (b == null) {
			return 1; // a > b
		}
		return a.compareTo(b);
	}

	/**
	 * Helper for compareTo implementations.
	 * Normalized negative number to -1 and positive non-zero numbers to 1.
	 *
	 * @param diff the value which shall be normalized
	 * @return see above
	 */
	public static int normalize(int diff) {
		return diff < 0 ? -1 : (diff == 0 ? 0 : 1);
	}

	/**
	 * Helper for compareTo implementations.
	 * Normalized negative number to -1 and positive non-zero numbers to 1.
	 *
	 * @param diff the value which shall be normalized
	 * @return see above
	 */
	public static int normalize(long diff) {
		return (diff < 0 ? -1 : (diff == 0 ? 0 : 1));
	}

	/**
	 * Helper for compareTo implementations.<br/>
	 * Boolean version which considers {@code false < true}
	 *
	 * <pre>
	 * a     b
	 * -----------------------
	 * true  true    =>  0
	 * true  false   =>  1
	 * false false   =>  0
	 * false true    => -1
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static int diff(boolean a, boolean b) {
		return (a == b ? 0 : (a ? 1 : -1));
	}

	/**
	 * Helper for compareTo implementations.<br/>
	 * Byte version which masks the input values so that an unsigned comparison is performed.
	 *
	 * <pre>
	 * Masks -128 -&gt; 127 =&gt; 0 -&gt; 255
	 * Maps &#x7b; a&lt;b, a==b, a&gt;b &#x7d; =&gt; &#x7b; -1, 0, 1 &#x7d;
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static int diff(byte a, byte b) {
		return diff(a & 0xFF, b & 0xFF);
	}

	/**
	 * Helper for compareTo implementations.
	 *
	 * <pre>
	 * Maps &#x7b; a&lt;b, a==b, a&gt;b &#x7d; =&gt; &#x7b; -1, 0, 1 &#x7d;
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static int diff(char a, char b) {
		return a == b ? 0 : (a < b ? -1 : 1);
	}

	/**
	 * Helper for compareTo implementations.
	 *
	 * <pre>
	 * Maps &#x7b; a&lt;b, a==b, a&gt;b &#x7d; => &#x7b; -1, 0, 1 &#x7d;
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static int diff(short a, short b) {
		return a == b ? 0 : (a < b ? -1 : 1);
	}

	/**
	 * Helper for compareTo implementations.
	 *
	 * <pre>
	 * Maps &#x7b; a&lt;b, a==b, a&gt;b &#x7d; =&gt; &#x7b; -1, 0, 1 &#x7d;
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static int diff(int a, int b) {
		return a == b ? 0 : (a < b ? -1 : 1);
	}

	/**
	 * Helper for compareTo implementations.
	 *
	 * <pre>
	 * Maps &#x7b; a&lt;b, a==b, a&gt;b &#x7d; =&gt; &#x7b; -1, 0, 1 &#x7d;
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static int diff(long a, long b) {
		return a == b ? 0 : (a < b ? -1 : 1);
	}

	/**
	 * Helper for compareTo implementations.
	 *
	 * <pre>
	 * Maps &#x7b; a&lt;b, a==b, a&gt;b &#x7d; =&gt; &#x7b; -1, 0, 1 &#x7d;
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static int diff(float a, float b) {
		return a == b ? 0 : (a < b ? -1 : 1);
	}

	/**
	 * Helper for compareTo implementations.
	 *
	 * <pre>
	 * Maps &#x7b; a&lt;b, a==b, a&gt;b &#x7d; =&gt; &#x7b; -1, 0, 1 &#x7d;
	 * </pre>
	 *
	 * @param a -
	 * @param b -
	 * @return see above
	 */
	public static int diff(double a, double b) {
		return a == b ? 0 : (a < b ? -1 : 1);
	}
}
