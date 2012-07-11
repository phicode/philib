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
 * Provides helper methods for hash-code generation.<br/>
 * Hash methods for handling all primitive types as well as <code>Object</code>s
 * are provided.<br/>
 * 
 * <b>Usage:</b>
 * 
 * <pre>
 * &#64;Override
 * public int hashCode() {
 *     int hash = HashUtil.startHash(field1);
 *     hash = HashUtil.nextHash(hash, field2);
 *     ...
 *     hash = HashUtil.nextHash(hash, fieldN);
 *     return hash;
 * }
 * </pre>
 * 
 * Note: fields may be null.
 * 
 * @author Philipp Meinen
 */
public final class HashUtil {

	private HashUtil() {
	}

	private static final int HASH_PRIME_START = 17;

	private static final int HASH_PRIME_STEP = 31;

	public static final int startHash(final Object obj) {
		return nextHash(HASH_PRIME_START, obj);
	}

	public static final int nextHash(int hash, final Object obj) {
		return hash * HASH_PRIME_STEP + ((obj == null) ? 0 : obj.hashCode());
	}

	public static final int startHash(final boolean value) {
		return nextHash(HASH_PRIME_START, value);
	}

	public static final int nextHash(int hash, final boolean value) {
		return hash * HASH_PRIME_STEP + (value ? 1 : 0);
	}

	public static final int startHash(final byte value) {
		return nextHash(HASH_PRIME_START, value);
	}

	public static final int nextHash(int hash, final byte value) {
		return hash * HASH_PRIME_STEP + value;
	}

	public static final int startHash(final char value) {
		return nextHash(HASH_PRIME_START, value);
	}

	public static final int nextHash(int hash, final char value) {
		return hash * HASH_PRIME_STEP + value;
	}

	public static final int startHash(final short value) {
		return nextHash(HASH_PRIME_START, value);
	}

	public static final int nextHash(int hash, final short value) {
		return hash * HASH_PRIME_STEP + value;
	}

	public static final int startHash(final int value) {
		return nextHash(HASH_PRIME_START, value);
	}

	public static final int nextHash(int hash, final int value) {
		return hash * HASH_PRIME_STEP + value;
	}

	public static final int startHash(final long value) {
		return nextHash(HASH_PRIME_START, value);
	}

	public static final int nextHash(int hash, final long value) {
		return hash * HASH_PRIME_STEP + hash(value);
	}

	public static final int startHash(final float value) {
		return nextHash(HASH_PRIME_START, value);
	}

	public static final int nextHash(final int hash, final float value) {
		return nextHash(hash, hash(value));
	}

	public static final int startHash(final double value) {
		return nextHash(HASH_PRIME_START, value);
	}

	public static final int nextHash(final int hash, final double value) {
		return nextHash(hash, hash(value));
	}

	public static final int hash(final long value) {
		return (int) (value ^ value >>> 32);
	}

	public static final int hash(final float value) {
		return Float.floatToIntBits(value);
	}

	public static final int hash(final double value) {
		return hash(Double.doubleToLongBits(value));
	}
}
