/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
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

import ch.bind.philib.io.BitOps;
import ch.bind.philib.io.EndianConverter;

/**
 * Implementation of the murmur hashing functions.
 * <p>
 * Based on Austin Appleby's <a href="http://code.google.com/p/smhasher">smhasher</a> public domain code.
 * </p>
 * 
 * @author Philipp Meinen
 */
public final class MurmurHash {

	private MurmurHash() {}

	private static final int MURMUR2_32_M = 0x5BD1E995;

	private static final int MURMUR2_32_R = 24;

	static final int MURMUR2_32_SEED = 0x9747B28C;

	private static final int MURMUR3_32_C1 = 0xCC9E2D51;

	private static final int MURMUR3_32_C2 = 0x1B873593;

	public static final int murmur2(byte[] key) {
		final int len = key.length;
		final int limitOffset = len & 0xFFFFFFFC;

		int off = 0;
		int hash = MURMUR2_32_SEED ^ len;

		while (off < limitOffset) {
			int k = EndianConverter.decodeInt32LE(key, off);
			off += 4;

			hash = murmur2_mmix(hash, k);
		}

		switch (len & 0x3) {
		case 3:
			hash ^= ((key[off + 2] & 0xFF) << 16);
		case 2:
			hash ^= ((key[off + 1] & 0xFF) << 8);
		case 1:
			hash ^= (key[off] & 0xFF);

			hash *= MURMUR2_32_M;
		}

		return murmur2_finalize(hash);
	}

	private static final int murmur2_finalize(int hash) {
		hash ^= (hash >>> 13);
		hash *= MURMUR2_32_M;
		hash ^= (hash >>> 15);
		return hash;
	}

	public static final int murmur3(byte[] key) {
		return murmur3(key, MURMUR2_32_SEED);
	}

	public static final int murmur3(byte[] key, int seed) {
		final int len = key.length;
		final int limitOffset = len & 0xFFFFFFFC;

		int off = 0;
		int hash = seed;

		while (off < limitOffset) {
			int k = EndianConverter.decodeInt32LE(key, off);
			off += 4;

			hash ^= murmur3_round32(k);

			hash = BitOps.rotl32(hash, 13);
			hash = (hash * 5) + 0xE6546B64;
		}

		int k = 0;
		switch (len & 0x3) {
		case 3:
			k ^= ((key[off + 2] & 0xFF) << 16);
		case 2:
			k ^= ((key[off + 1] & 0xFF) << 8);
		case 1:
			k ^= (key[off] & 0xFF);

			hash ^= murmur3_round32(k);
		}

		hash ^= len;
		hash = murmur3_fmix32(hash);
		return hash;
	}

	// murmur3_start_8bit(byte v)

	private static final int murmur3_round32(int k) {
		k *= MURMUR3_32_C1;
		k = BitOps.rotl32(k, 15);
		k *= MURMUR3_32_C2;
		return k;
	}

	private static final int murmur3_fmix32(int hash) {
		hash ^= hash >>> 16;
		hash *= 0x85EBCA6B;
		hash ^= hash >>> 13;
		hash *= 0xC2B2AE35;
		hash ^= hash >>> 16;
		return hash;
	}

	public static final long optimize() {
		byte[] b = {
				1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
		final long s = System.nanoTime();
		for (int i = 0; i < 12000; i++) {
			murmur2(b);
			murmur3(b);
		}
		return (System.nanoTime() - s);
	}

	// This is a variant of MurmurHash2 modified to use the Merkle-Damgard
	// construction.
	public static int murmur2a(int hash, byte[] key) {
		final int len = key.length;
		final int limitOffset = len & 0xFFFFFFFC;

		int off = 0;

		while (off < limitOffset) {
			int k = EndianConverter.decodeInt32LE(key, off);
			off += 4;

			hash = murmur2_mmix(hash, k);
		}

		int t = 0;
		switch (len & 0x3) {
		case 3:
			t ^= ((key[off + 2] & 0xFF) << 16);
		case 2:
			t ^= ((key[off + 1] & 0xFF) << 8);
		case 1:
			t ^= (key[off] & 0xFF);
		}

		hash = murmur2_mmix(hash, t);
		hash = murmur2_mmix(hash, len);

		return murmur2_finalize(hash);
	}

	private static final int murmur2_mmix(int hash, int k) {
		k *= MURMUR2_32_M;
		k ^= (k >>> MURMUR2_32_R);
		k *= MURMUR2_32_M;

		hash *= MURMUR2_32_M;
		hash ^= k;
		return hash;
	}

	public static int murmur2a_8bit(int hash, int v) {
		hash = murmur2_mmix(hash, v); // t / remaining
		hash = murmur2_mmix(hash, 1); // len

		return murmur2_finalize(hash);
	}

	public static int murmur2a_16bit(int hash, int v) {
		hash = murmur2_mmix(hash, v); // t / remaining
		hash = murmur2_mmix(hash, 2); // len

		return murmur2_finalize(hash);
	}

	public static int murmur2a_32bit(int hash, int v) {
		hash = murmur2_mmix(hash, v);

		hash = murmur2_mmix(hash, 0); // t / remaining
		hash = murmur2_mmix(hash, 4); // len

		return murmur2_finalize(hash);
	}

	public static int murmur2a_64bit(int hash, long v) {
		// as in little-endian: LSB before MSB
		int lsb = (int) (v & 0xFFFFFFFF);
		int msb = (int) (v >>> 32);
		hash = murmur2_mmix(hash, lsb);
		hash = murmur2_mmix(hash, msb);

		hash = murmur2_mmix(hash, 0); // t==0
		hash = murmur2_mmix(hash, 4); // len==4

		return murmur2_finalize(hash);
	}
}
