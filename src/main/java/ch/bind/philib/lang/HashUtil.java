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

import static ch.bind.philib.lang.MurmurHash.MURMUR2_32_SEED;
import static ch.bind.philib.lang.MurmurHash.murmur2a_16bit;
import static ch.bind.philib.lang.MurmurHash.murmur2a_32bit;
import static ch.bind.philib.lang.MurmurHash.murmur2a_64bit;
import static ch.bind.philib.lang.MurmurHash.murmur2a_8bit;

/**
 * Provides helper methods for hash-code generation.<br/>
 * Hash methods for handling all primitive types as well as <code>Object</code>s are provided.<br/>
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

	private HashUtil() {}

	public static final int startHash(final boolean value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static final int startHash(final byte value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static final int startHash(final char value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static final int startHash(final short value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static final int startHash(final int value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static final int startHash(final long value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static final int startHash(final float value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static final int startHash(final double value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static final int startHash(final Object obj) {
		return nextHash(MURMUR2_32_SEED, obj);
	}

	public static final int nextHash(int hash, final boolean value) {
		return murmur2a_8bit(hash, (value ? 1 : 0));
	}

	public static final int nextHash(int hash, final byte value) {
		return murmur2a_8bit(hash, (value & 0xFF));
	}

	public static final int nextHash(int hash, final char value) {
		return murmur2a_16bit(hash, value);
	}

	public static final int nextHash(int hash, final short value) {
		return murmur2a_16bit(hash, value);
	}

	public static final int nextHash(int hash, final int value) {
		return murmur2a_32bit(hash, value);
	}

	public static final int nextHash(int hash, final long value) {
		return murmur2a_64bit(hash, value);
	}

	public static final int nextHash(final int hash, final float value) {
		return nextHash(hash, fromFloat(value));
	}

	public static final int nextHash(final int hash, final double value) {
		return nextHash(hash, fromDouble(value));
	}

	public static final int nextHash(int hash, final Object obj) {
		int objHash = ((obj == null) ? 0 : obj.hashCode());
		return murmur2a_32bit(hash, objHash);
	}

	public static final int fromFloat(final float value) {
		return Float.floatToIntBits(value);
	}

	public static final long fromDouble(final double value) {
		return Double.doubleToLongBits(value);
	}
}
