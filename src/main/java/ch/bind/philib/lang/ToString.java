/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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

public final class ToString {

	private ToString() {
	}

	public static StringBuilder start(final Object obj) {
		final StringBuilder sb = new StringBuilder();
		sb.append(obj.getClass().getSimpleName());
		sb.append('[');
		return sb;
	}

	public static String end(final StringBuilder sb) {
		return sb.append(']').toString();
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final Object obj) {
		sb.append(name).append('=').append(obj);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final Object obj) {
		sb.append(obj);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final boolean val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final boolean val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final char val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final char val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final int val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final int val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final long val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final long val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final float val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final float val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final double val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final double val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final Object obj) {
		sb.append(", ").append(name).append('=').append(obj);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final Object obj) {
		sb.append(", ").append(obj);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final boolean val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final boolean val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final char val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final char val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final int val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final int val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final long val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final long val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final float val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final float val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final double val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final double val) {
		sb.append(", ").append(val);
		return sb;
	}
}
