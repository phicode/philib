/*
 * Copyright (c) 2006-2009 Philipp Meinen <philipp@bind.ch>
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a copy
 *	of this software and associated documentation files (the "Software"),
 *	to deal in the Software without restriction, including without limitation
 *	the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *	and/or sell copies of the Software, and to permit persons to whom the Software
 *	is furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included
 *	in all copies or substantial portions of the Software.

 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *	IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *	DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *	TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 *	THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.bind.philib.data;

public final class HashUtil {

	private HashUtil() {
	}

	private static final int HASH_PRIME = 31;

	public static final int nextHash(int hash, final Object obj) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + ((obj == null) ? 0 : obj.hashCode());
	}

	public static final int nextHash(int hash, final byte value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + (int)value;
	}

	public static final int nextHash(int hash, final char value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + value;
	}

	public static final int nextHash(int hash, final short value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + value;
	}

	public static final int nextHash(int hash, final int value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + value;
	}

	public static final int nextHash(int hash, final float value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + Float.valueOf(value).hashCode();
	}

	public static final int nextHash(int hash, final double value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + Double.valueOf(value).hashCode();
	}
}
