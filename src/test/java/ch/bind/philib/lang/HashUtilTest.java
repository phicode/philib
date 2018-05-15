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

import static ch.bind.philib.lang.HashUtil.nextHash;
import static ch.bind.philib.lang.HashUtil.startHash;
import static ch.bind.philib.lang.MurmurHash.MURMUR2_32_SEED;
import static ch.bind.philib.lang.MurmurHash.murmur2a;
import static ch.bind.philib.lang.MurmurHash.murmur2a_32bit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.bind.philib.TestUtil;

public class HashUtilTest {

	private final int HASH_4BYTE_ZERO = murmur2a_32bit(MURMUR2_32_SEED, 0);

	private static void verifyStringHashNonCommutative(String a, String b) {
		assertNotNull(a);
		assertNotNull(b);
		assertFalse(a.isEmpty());
		assertFalse(b.isEmpty());

		int h1 = nextHash(startHash(a), b);
		int h2 = nextHash(startHash(b), a);
		assertTrue(h1 != h2);
	}

	private static int hash(long a, int b, short c, byte d) {
		int h = HashUtil.startHash(a);
		h = HashUtil.nextHash(h, b);
		h = HashUtil.nextHash(h, c);
		h = HashUtil.nextHash(h, d);
		return h;
	}

	@Test
	public void simpleByte() {
		for (byte a = -128; a < 127; a++) {
			for (short b = -128; b <= 127; b++) {
				if (a != b) {
					int h1 = nextHash(startHash(a), b);
					int h2 = nextHash(startHash(b), a);
					assertTrue(h1 != h2);
				}
			}
		}
	}

	@Test
	public void simpleChar() {
		int a = nextHash(startHash('a'), 'a');
		int b = nextHash(startHash('a'), 'b');
		int c = nextHash(startHash('b'), 'a');
		int d = nextHash(startHash('b'), 'b');
		assertTrue(a != b && a != c && a != d);
		assertTrue(b != c && b != d);
		assertTrue(c != d);
	}

	@Test
	public void simpleShort() {
		for (short a = -500; a < 500; a++) {
			for (short b = -500; b < 500; b++) {
				if (a != b) {
					int h1 = nextHash(startHash(a), b);
					int h2 = nextHash(startHash(b), a);
					assertTrue(h1 != h2);
				}
			}
		}
	}

	@Test
	public void simpleInt() {
		for (int a = 0; a < 1000; a++) {
			for (int b = 0; b < 1000; b++) {
				if (a != b) {
					int h1 = nextHash(startHash(a), b);
					int h2 = nextHash(startHash(b), a);
					assertTrue(h1 != h2);
				}
			}
		}
	}

	@Test
	public void simpleLong() {
		for (long a = 0; a < 1000; a++) {
			for (long b = 0; b < 1000; b++) {
				if (a != b) {
					int h1 = nextHash(startHash(a), b);
					int h2 = nextHash(startHash(b), a);
					assertTrue(h1 != h2);
				}
			}
		}
	}

	@Test
	public void floatHashes() {
		float value = 0;
		int expected = HashUtil.startHash(Float.floatToIntBits(value));

		int hash = HashUtil.startHash(value);
		assertEquals(expected, hash);

		value = Float.MIN_VALUE;
		hash = HashUtil.nextHash(hash, value);
		expected = HashUtil.nextHash(expected, Float.floatToIntBits(value));
		assertEquals(expected, hash);

		value = Float.MAX_VALUE;
		hash = HashUtil.nextHash(hash, value);
		expected = HashUtil.nextHash(expected, Float.floatToIntBits(value));
		assertEquals(expected, hash);
	}

	@Test
	public void doubleHashes() {
		double value = 0;
		int expected = HashUtil.startHash(Double.doubleToLongBits(value));

		int hash = HashUtil.startHash(value);
		assertEquals(expected, hash);

		value = Double.MIN_VALUE;
		hash = HashUtil.nextHash(hash, value);
		expected = HashUtil.nextHash(expected, Double.doubleToLongBits(value));
		assertEquals(expected, hash);

		value = Double.MAX_VALUE;
		hash = HashUtil.nextHash(hash, value);
		expected = HashUtil.nextHash(expected, Double.doubleToLongBits(value));
		assertEquals(expected, hash);
	}

	@Test
	public void bool() {
		int a = nextHash(startHash(true), true);
		int b = nextHash(startHash(true), false);
		int c = nextHash(startHash(false), true);
		int d = nextHash(startHash(false), false);
		assertTrue(a != b && a != c && a != d);
		assertTrue(b != c && b != d);
		assertTrue(c != d);
	}

	@Test
	public void nullObj() {
		Object nil = null;
		Object o = "a";
		int a = nextHash(startHash(o), nil);
		int b = nextHash(startHash(o), o);
		int c = nextHash(startHash(nil), nil);
		int d = nextHash(startHash(nil), o);
		assertTrue(a != b && a != c && a != d);
		assertTrue(b != c && b != d);
		assertTrue(c != d);
	}

	@Test
	public void naiveStrings() {
		List<String> wordlist = TestUtil.getWordlist();
		String a = null;
		for (String b : wordlist) {
			if (a != null) {
				verifyStringHashNonCommutative(a, b);
			}
			a = b;
		}
	}

	@Test(enabled = false)
	public void differentHashes() {
		// all permutations of a:64b, b:32b, c:16b, d:8b
		// where a := [ 3 .. 300 @ step 3 ]
		// where b := [ 8 .. 800 @ step 8 ]
		// where c := [ 9 .. 900 @ step 9 ]
		// where d := [ 1 .. 255 @ step 2]
		// space: 100 * 100 * 100 * 128 = 128'000'000 * 4B = 512MiB
		final int n = 100 * 100 * 100 * 128;
		final int[] results = new int[n];
		int idx = 0;
		final long tStart = System.currentTimeMillis();
		for (long a = 3; a <= 300; a += 3) {
			for (int b = 8; b <= 800; b += 8) {
				for (short c = 9; c <= 900; c = (short) (c + 9)) {
					for (int id = 1; id < 256; id += 2) {
						byte d = (byte) (id & 0xFF);
						int hc = hash(a, b, c, d);
						results[idx++] = hc;
					}
				}
			}
		}
		assertEquals(idx, n);
		final long t0 = System.currentTimeMillis() - tStart;
		Arrays.sort(results);
		final long t1 = System.currentTimeMillis() - tStart - t0;
		int prev = results[0];
		int colls = 0;
		int colCount = 0;
		int highColl = 0;
		for (int i = 1; i < n; i++) {
			int cur = results[i];
			if (cur == prev) {
				colls++;
				colCount++;
			} else {
				if (colCount > highColl) {
					highColl = colCount;
				}
				colCount = 0;
			}
			prev = cur;
			cur++;
		}
		final long t2 = System.currentTimeMillis() - tStart - t0 - t1;
		double collFactor = ((double) colls) / ((double) n);
		// less than 1.5%
		assertTrue(collFactor < 0.015);
		System.out.printf("t0=%d, t1=%d, t2=%d, collisions=%d/%d, highColl=%d\n", t0, t1, t2, colls, n, highColl);

		// for (long a = 3; a <= 300; a += 3) {
		// for (int b = 8; b <= 800; b += 8) {
		// for (short c = 9; c <= 900; c += 9) {
		// for (int id = 1; id < 256; id += 2) {
		// byte d = (byte) (id & 0xFF);
		// int hc = hash(a, b, c, d);
		// if (Arrays.binarySearch(results, hc) >= 0) {
		// System.out.printf("collision with %d,%d,%d,%d\n", a, b, c, d);
		// }
		// }
		// }
		// }
		// }
		// final long t3 = System.currentTimeMillis() - tStart - t0 - t1 - t2;
		// System.out.printf("t0=%d, t1=%d, t2=%d,t3=%d, collisions=%d/%d\n",
		// t0, t1, t2, t3, colls, n);
	}

	@Test
	public void arrayNilOrEmpty() {
		assertEquals(startHash(ArrayUtil.EMPTY_BOOL_ARRAY), HASH_4BYTE_ZERO);
		assertEquals(startHash((boolean[]) null), HASH_4BYTE_ZERO);

		assertEquals(startHash(ArrayUtil.EMPTY_BYTE_ARRAY), HASH_4BYTE_ZERO);
		assertEquals(startHash((byte[]) null), HASH_4BYTE_ZERO);

		assertEquals(startHash(ArrayUtil.EMPTY_CHAR_ARRAY), HASH_4BYTE_ZERO);
		assertEquals(startHash((char[]) null), HASH_4BYTE_ZERO);

		assertEquals(startHash(ArrayUtil.EMPTY_SHORT_ARRAY), HASH_4BYTE_ZERO);
		assertEquals(startHash((short[]) null), HASH_4BYTE_ZERO);

		assertEquals(startHash(ArrayUtil.EMPTY_INT_ARRAY), HASH_4BYTE_ZERO);
		assertEquals(startHash((int[]) null), HASH_4BYTE_ZERO);

		assertEquals(startHash(ArrayUtil.EMPTY_LONG_ARRAY), HASH_4BYTE_ZERO);
		assertEquals(startHash((long[]) null), HASH_4BYTE_ZERO);

		assertEquals(startHash(ArrayUtil.EMPTY_FLOAT_ARRAY), HASH_4BYTE_ZERO);
		assertEquals(startHash((float[]) null), HASH_4BYTE_ZERO);

		assertEquals(startHash(ArrayUtil.EMPTY_DOUBLE_ARRAY), HASH_4BYTE_ZERO);
		assertEquals(startHash((double[]) null), HASH_4BYTE_ZERO);

		assertEquals(startHash(ArrayUtil.EMPTY_OBJECT_ARRAY), HASH_4BYTE_ZERO);
		assertEquals(startHash((Object[]) null), HASH_4BYTE_ZERO);
	}

	@Test
	public void arrayLengthOne() {
		assertEquals(startHash(new boolean[]{true}), nextHash(startHash(1), true));
		assertEquals(startHash(new boolean[]{false}), nextHash(startHash(1), false));

		assertEquals(startHash(new byte[]{0x5A}), nextHash(startHash(1), (byte) 0x5A));
		assertEquals(startHash(new char[]{0x5A5A}), nextHash(startHash(1), (char) 0x5A5A));
		assertEquals(startHash(new short[]{0x5A5A}), nextHash(startHash(1), (short) 0x5A5A));
		assertEquals(startHash(new int[]{0x5A5A5A5A}), nextHash(startHash(1), 0x5A5A5A5A));
		assertEquals(startHash(new long[]{0x5A5A5A5A5A5A5A5AL}), nextHash(startHash(1), 0x5A5A5A5A5A5A5A5AL));
		assertEquals(startHash(new float[]{(float) Math.PI}), nextHash(startHash(1), (float) Math.PI));
		assertEquals(startHash(new double[]{Math.PI}), nextHash(startHash(1), Math.PI));
		assertEquals(startHash(new Object[]{"test"}), nextHash(startHash(1), "test"));
	}

	@Test
	public void arrayLengthTwo() {
		assertEquals(startHash(new boolean[]{true, false}), nextHash(nextHash(startHash(2), true), false));
		assertEquals(startHash(new boolean[]{false, true}), nextHash(nextHash(startHash(2), false), true));

		assertEquals(startHash(new byte[]{0x5A, 0x3C}), murmur2a(startHash(2), new byte[]{0x5A, 0x3C}));
		assertEquals(startHash(new char[]{0x5AA5, 0x3C3C}), nextHash(nextHash(startHash(2), (char) 0x5AA5), (char) 0x3C3C));
		assertEquals(startHash(new short[]{0x5A5A, 0x3C3C}), nextHash(nextHash(startHash(2), (short) 0x5A5A), (short) 0x3C3C));
		assertEquals(startHash(new int[]{0x5A5A5A5A, 0x3C3C3C3C}), nextHash(nextHash(startHash(2), 0x5A5A5A5A), 0x3C3C3C3C));
		assertEquals(startHash(new long[]{0x5A5A5A5A5A5A5A5AL, 0x3C3C3C3C3C3C3C3CL}), nextHash(nextHash(startHash(2), 0x5A5A5A5A5A5A5A5AL), 0x3C3C3C3C3C3C3C3CL));
		assertEquals(startHash(new float[]{(float) Math.PI, (float) Math.E}), nextHash(nextHash(startHash(2), (float) Math.PI), (float) Math.E));
		assertEquals(startHash(new double[]{Math.PI, Math.E}), nextHash(nextHash(startHash(2), Math.PI), Math.E));
		assertEquals(startHash(new Object[]{"a", "b"}), nextHash(nextHash(startHash(2), "a"), "b"));
	}
}
