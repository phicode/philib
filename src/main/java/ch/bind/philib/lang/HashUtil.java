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
import static ch.bind.philib.lang.MurmurHash.murmur2a;
import static ch.bind.philib.lang.MurmurHash.murmur2a_16bit;
import static ch.bind.philib.lang.MurmurHash.murmur2a_32bit;
import static ch.bind.philib.lang.MurmurHash.murmur2a_64bit;
import static ch.bind.philib.lang.MurmurHash.murmur2a_8bit;

/**
 * Provides helper methods for hash-code generation.<br/>
 * Hash methods for handling all primitive types as well as <code>Object</code>s are provided.<br/>
 * <b>Usage:</b>
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
 * Note: fields may be null.
 *
 * @author Philipp Meinen
 */
public abstract class HashUtil {

	protected HashUtil() {
	}

	public static int startHash(final boolean value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static int startHash(final byte value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static int startHash(final char value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static int startHash(final short value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static int startHash(final int value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static int startHash(final long value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static int startHash(final float value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static int startHash(final double value) {
		return nextHash(MURMUR2_32_SEED, value);
	}

	public static int startHash(final Object obj) {
		return nextHash(MURMUR2_32_SEED, obj);
	}

	public static int startHash(final boolean[] values) {
		return nextHash(MURMUR2_32_SEED, values);
	}

	public static int startHash(final byte[] values) {
		return nextHash(MURMUR2_32_SEED, values);
	}

	public static int startHash(final char[] values) {
		return nextHash(MURMUR2_32_SEED, values);
	}

	public static int startHash(final short[] values) {
		return nextHash(MURMUR2_32_SEED, values);
	}

	public static int startHash(final int[] values) {
		return nextHash(MURMUR2_32_SEED, values);
	}

	public static int startHash(final long[] values) {
		return nextHash(MURMUR2_32_SEED, values);
	}

	public static int startHash(final float[] values) {
		return nextHash(MURMUR2_32_SEED, values);
	}

	public static int startHash(final double[] values) {
		return nextHash(MURMUR2_32_SEED, values);
	}

	public static int startHash(final Object[] objs) {
		return nextHash(MURMUR2_32_SEED, objs);
	}

	public static int nextHash(int hash, final boolean value) {
		return murmur2a_8bit(hash, (value ? 1 : 0));
	}

	public static int nextHash(int hash, final byte value) {
		return murmur2a_8bit(hash, (value & 0xFF));
	}

	public static int nextHash(int hash, final char value) {
		return murmur2a_16bit(hash, value);
	}

	public static int nextHash(int hash, final short value) {
		return murmur2a_16bit(hash, value);
	}

	public static int nextHash(int hash, final int value) {
		return murmur2a_32bit(hash, value);
	}

	public static int nextHash(int hash, final long value) {
		return murmur2a_64bit(hash, value);
	}

	public static int nextHash(final int hash, final float value) {
		return nextHash(hash, fromFloat(value));
	}

	public static int nextHash(final int hash, final double value) {
		return nextHash(hash, fromDouble(value));
	}

	public static int nextHash(int hash, final Object obj) {
		int objHash = ((obj == null) ? 0 : obj.hashCode());
		return murmur2a_32bit(hash, objHash);
	}

	public static int nextHash(int hash, final boolean[] values) {
		if (values == null || values.length == 0) {
			return murmur2a_32bit(hash, 0);
		}
		hash = murmur2a_32bit(hash, values.length);
		for (boolean value : values) {
			hash = nextHash(hash, value);
		}
		return hash;
	}

	public static int nextHash(int hash, final byte[] values) {
		if (values == null || values.length == 0) {
			return murmur2a_32bit(hash, 0);
		}
		hash = murmur2a_32bit(hash, values.length);
		return murmur2a(hash, values);
	}

	public static int nextHash(int hash, final char[] values) {
		if (values == null || values.length == 0) {
			return murmur2a_32bit(hash, 0);
		}
		hash = murmur2a_32bit(hash, values.length);
		for (char value : values) {
			hash = nextHash(hash, value);
		}
		return hash;
	}

	public static int nextHash(int hash, final short[] values) {
		if (values == null || values.length == 0) {
			return murmur2a_32bit(hash, 0);
		}
		hash = murmur2a_32bit(hash, values.length);
		for (short value : values) {
			hash = nextHash(hash, value);
		}
		return hash;
	}

	public static int nextHash(int hash, final int[] values) {
		if (values == null || values.length == 0) {
			return murmur2a_32bit(hash, 0);
		}
		hash = murmur2a_32bit(hash, values.length);
		for (int value : values) {
			hash = nextHash(hash, value);
		}
		return hash;
	}

	public static int nextHash(int hash, final long[] values) {
		if (values == null || values.length == 0) {
			return murmur2a_32bit(hash, 0);
		}
		hash = murmur2a_32bit(hash, values.length);
		for (long value : values) {
			hash = nextHash(hash, value);
		}
		return hash;
	}

	public static int nextHash(int hash, final float[] values) {
		if (values == null || values.length == 0) {
			return murmur2a_32bit(hash, 0);
		}
		hash = murmur2a_32bit(hash, values.length);
		for (float value : values) {
			hash = nextHash(hash, value);
		}
		return hash;
	}

	public static int nextHash(int hash, final double[] values) {
		if (values == null || values.length == 0) {
			return murmur2a_32bit(hash, 0);
		}
		hash = murmur2a_32bit(hash, values.length);
		for (double value : values) {
			hash = nextHash(hash, value);
		}
		return hash;
	}

	public static int nextHash(int hash, final Object[] objects) {
		if (objects == null || objects.length == 0) {
			return murmur2a_32bit(hash, 0);
		}
		hash = murmur2a_32bit(hash, objects.length);
		for (Object object : objects) {
			hash = nextHash(hash, object);
		}
		return hash;
	}

	public static int fromFloat(final float value) {
		return Float.floatToIntBits(value);
	}

	public static long fromDouble(final double value) {
		return Double.doubleToLongBits(value);
	}
}
