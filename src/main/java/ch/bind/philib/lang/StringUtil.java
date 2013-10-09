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

package ch.bind.philib.lang;

/**
 * @author Philipp Meinen
 */
public abstract class StringUtil {

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	protected StringUtil() {
	}

	public static String extractBack(String s, char delim) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		final int l = s.length();
		int start = 0;
		for (int i = 0; i < l; i++) {
			char c = s.charAt(i);
			if (c == delim) {
				start = i + 1;
			}
		}
		if (start == l) {
			return "";
		}
		return s.substring(start, l);
	}

	public static StringBuilder start(Object obj) {
		StringBuilder sb = new StringBuilder();
		sb.append(obj.getClass().getSimpleName());
		sb.append('[');
		return sb;
	}

	public static String end(StringBuilder sb) {
		return sb.append(']').toString();
	}

	public static void firstObj(StringBuilder sb, String name, Object obj) {
		sb.append(name);
		sb.append('=');
		sb.append(obj);
	}

	public static void firstObj(StringBuilder sb, Object obj) {
		sb.append(obj);
	}

	public static void addObj(StringBuilder sb, String name, Object obj) {
		sb.append(", ");
		firstObj(sb, name, obj);
	}

	public static void addObj(StringBuilder sb, Object obj) {
		sb.append(", ");
		sb.append(obj);
	}

	public static void firstInt(StringBuilder sb, String name, int v) {
		sb.append(name);
		sb.append('=');
		sb.append(v);
	}

	public static void addInt(StringBuilder sb, String name, int v) {
		sb.append(", ");
		firstInt(sb, name, v);
	}

	public static void addInt(StringBuilder sb, int v) {
		sb.append(", ");
		sb.append(v);
	}

	public static void firstLong(StringBuilder sb, String name, long v) {
		sb.append(name);
		sb.append('=');
		sb.append(v);
	}

	public static void addLong(StringBuilder sb, String name, long v) {
		sb.append(", ");
		firstLong(sb, name, v);
	}

	public static void addLong(StringBuilder sb, long v) {
		sb.append(", ");
		sb.append(v);
	}

	/**
	 * Counts the number of times the character with the given unicode {@code value} occurs.
	 * 
	 * @param str the input string, may be null or empty
	 * @param value the unicode value to search for
	 * @return the number of times the given unicode {@code value} occurs.
	 */
	public static int count(String str, int value) {
		if (str == null || str.length() == 0) {
			return 0;
		}
		int n = 0;
		int fromIdx = 0;
		while (true) {
			int off = str.indexOf(value, fromIdx);
			if (off != -1) {
				fromIdx = off + 1;
				n++;
			} else {
				break;
			}
		}
		return n;
	}

	/**
	 * Splits a string by a given unicode {@code delimiter}.
	 * 
	 * @param str the input string, may be null or empty
	 * @param delimimiter the delimiter to use
	 * @return A string array with the different parts of the string after splitting it. The
	 *         returned array is never null but may be empty (length 0). However, none of the
	 *         returned values is empty.
	 */
	public static String[] split(String str, int delimimiter) {
		if (str == null) {
			return EMPTY_STRING_ARRAY;
		}
		final int len = str.length();
		if (len == 0) {
			return EMPTY_STRING_ARRAY;
		}
		final int n = count(str, delimimiter);
		if (n == 0) {
			return new String[] { str };
		}
		// make room for the case where no two delimiters follow each other
		String[] parts = new String[n + 1];
		int i = 0;
		int fromIdx = 0;
		while (true) {
			int off = str.indexOf(delimimiter, fromIdx);
			int partLen;
			if (off == -1) {
				partLen = len - fromIdx;
			} else {
				partLen = off - fromIdx;
			}
			if (partLen > 0) {
				parts[i++] = str.substring(fromIdx, fromIdx + partLen);
			}
			if (off == -1) {
				break;
			}
			fromIdx = off + 1;
		}
		if (i == parts.length) {
			return parts;
		}
		String[] rv = new String[i];
		System.arraycopy(parts, 0, rv, 0, i);
		return rv;
	}
}
