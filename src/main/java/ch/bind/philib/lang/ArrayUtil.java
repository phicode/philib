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

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Various functions for dealing with arrays which are not present in the standard
 * {@link java.util.Arrays} class.
 * 
 * @author Philipp Meinen
 * @since 2009-06-10
 */
public abstract class ArrayUtil {

	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	protected ArrayUtil() {
	}

	// java.util.Random is updated atomically => this is thread-safe
	private static final Random rand = new Random();

	/**
	 * Fills the <code>destination</code> array with randomly picked values from the
	 * <code>source</code> array. No value
	 * will be picked twice.
	 * 
	 * @param source The array from which random values must be picked. The content of this array
	 *            will not be altered.
	 * @param destination The array which must be filled with random values. Previous values within
	 *            this array will be
	 *            overwritten.
	 * @throws NullPointerException If either of the two parameters is null.
	 * @throws IllegalArgumentException If the <code>source</code>-array is smaller then the
	 *             <code>destination</code> -array.
	 */
	public static <T> void pickRandom(final T[] source, final T[] destination) {
		if (source == null)
			throw new NullPointerException("the source array must not be null");
		if (destination == null)
			throw new NullPointerException("the destination array must not be null");
		final int nSrc = source.length;
		final int nDst = destination.length;
		if (nSrc < nDst)
			throw new IllegalArgumentException("the source arrays length must be greater or equal to the destination arrays length");
		final boolean[] taken = new boolean[nSrc];

		for (int i = 0; i < nDst; i++) {
			int idx = rand.nextInt(nSrc);
			while (taken[idx])
				idx = rand.nextInt(nSrc);
			taken[idx] = true;
			destination[i] = source[idx];
		}
	}

	/**
	 * concatenate the content of two byte arrays.
	 * 
	 * @param a the first byte array (may be null)
	 * @param b the second byte array (may be null)
	 * @return a new byte array with the combined length of {@code a} and {@code b}, containing a
	 *         copy of their content.
	 */
	public static byte[] concat(byte[] a, byte[] b) {
		// override null arrays
		if (a == null) {
			a = EMPTY_BYTE_ARRAY;
		}
		if (b == null) {
			b = EMPTY_BYTE_ARRAY;
		}
		int la = a.length, lb = b.length;
		int len = la + lb;
		byte[] rv = new byte[len];
		System.arraycopy(a, 0, rv, 0, la);
		System.arraycopy(b, 0, rv, la, lb);
		return rv;
	}

	/**
	 * append the content of two byte arrays up to a certain capacity limit
	 * 
	 * @param a the first byte array (may be null)
	 * @param b the second byte array (may be null)
	 * @return a new byte array with the combined length of {@code a} and {@code b}, containing a
	 *         copy of their content. if the combined length exceeds {@code capacity} the returned
	 *         array {@code a} will have {@code a.length == capacity}.
	 */
	public static byte[] append(byte[] a, byte[] b, int capacity) {
		// override null arrays
		if (capacity <= 0) {
			return EMPTY_BYTE_ARRAY;
		}
		if (a == null)
			a = EMPTY_BYTE_ARRAY;
		if (b == null)
			b = EMPTY_BYTE_ARRAY;
		int la = a.length, lb = b.length;
		int len = la + lb;
		len = Math.min(len, capacity);
		byte[] rv = new byte[len];
		if (la >= capacity) {
			System.arraycopy(a, 0, rv, 0, capacity);
		} else {
			System.arraycopy(a, 0, rv, 0, la);
			int fromB = Math.min(capacity - la, lb);
			System.arraycopy(b, 0, rv, la, fromB);
		}
		return rv;
	}

	public static byte[] extractBack(byte[] data, int len) {
		byte[] rv = new byte[len];
		int offset = data.length - len;
		System.arraycopy(data, offset, rv, 0, len);
		return rv;
	}

	public static byte[] extractFront(byte[] data, int len) {
		byte[] rv = new byte[len];
		System.arraycopy(data, 0, rv, 0, len);
		return rv;
	}

	public static String formatShortHex(byte[] data) {
		if (data == null || data.length == 0) {
			return "";
		}
		return formatShortHex(data, 0, data.length);
	}

	public static String formatShortHex(byte[] data, int off, int len) {
		if (data == null || data.length == 0) {
			return "";
		}
		final int l = data.length;

		StringBuilder sb = new StringBuilder(len * 2);
		for (int i = 0; i < len; i++) {
			int idx = off + i;
			if (idx >= l) {
				break;
			}
			toShortHex(sb, (data[idx] & 0xFF));
		}
		return sb.toString();
	}

	public static String formatShortHex(ByteBuffer data) {
		if (data == null) {
			return "";
		}
		final int len = data.remaining();
		if (len == 0) {
			return "";
		}
		if (data.hasArray()) {
			return formatShortHex(data.array(), data.position(), len);
		}
		StringBuilder sb = new StringBuilder(len * 2);
		final int initialPos = data.position();
		for (int i = 0; i < len; i++) {
			toShortHex(sb, (data.get() & 0xFF));
		}
		data.position(initialPos);
		return sb.toString();
	}

	private static void toShortHex(StringBuilder sb, int v) {
		assert (v >= 0 && v < 256);
		if (v < 16) {
			sb.append('0');
		} else {
			sb.append(TO_HEX[v >>> 4]);
		}
		sb.append(TO_HEX[v & 15]);
	}

	private static final char[] TO_HEX = { '0', '1', '2', '3', //
	        '4', '5', '6', '7', //
	        '8', '9', 'A', 'B', //
	        'C', 'D', 'E', 'F' };
}
