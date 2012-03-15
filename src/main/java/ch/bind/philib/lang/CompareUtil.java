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
public final class CompareUtil {

	private CompareUtil() {
	}

	/**
	 * A null-safe equality checking method.<br/>
	 * results:
	 * 
	 * <pre>
	 *  a / b | null  | a         |
	 * ------------------------------
	 *  null  | true  | false       |
	 *     b  | false | a.equals(b) |
	 * </pre>
	 * 
	 * @param a
	 *            -
	 * @param b
	 *            -
	 * @return see above
	 */
	public static boolean equals(final Object a, final Object b) {
		if (a == b)
			return true;
		if (a == null) {
			// b is not null -> not equal
			return false;
		} else {
			if (b == null) {
				return false;
			} else {
				return a.equals(b);
			}
		}
	}

	public static <T> int compare(final Comparable<T> a, final T b) {
		if (a == b)
			return 0;
		if (a == null) {
			// b is not null
			return -1; // a < b
		} else {
			if (b == null) {
				return 1; // a > b
			} else {
				return a.compareTo(b);
			}
		}
	}

	public static int compareBool(boolean a, boolean b) {
		return (a == b ? 0 : (a ? 1 : -1));
	}

	public static final int normalize(int diff) {
		return diff < 0 ? -1 : (diff == 0 ? 0 : 1);
	}

	public static final int normalize(long diff) {
		return (diff < 0 ? -1 : (diff == 0 ? 0 : 1));
	}
}
