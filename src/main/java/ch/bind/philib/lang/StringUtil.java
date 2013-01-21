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

	protected StringUtil() {}

	public static String extractBack(String s, char delim) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		final int l = s.length();
		int start = 0;
		for (int i = 0; i < l; i++) {
			char c = s.charAt(i);
			if (c == delim) {
				start = i+1;
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
}
