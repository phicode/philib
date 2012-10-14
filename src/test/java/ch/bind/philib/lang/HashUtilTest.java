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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

public class HashUtilTest {

	@Test(enabled = false)
	public void objectHashes() {
		Integer value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.startHash(value);
		assertEquals(expected, hash);

		value = (int) 4294967295L; // 2^32-1
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 1;
		assertEquals(expected, hash);

		value = (int) 2147483647L; // 2^31-1
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 2147483647;
		assertEquals(expected, hash);

		value = (int) 2147483648L; // 2^31
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + -2147483648;
		assertEquals(expected, hash);

		value = null;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31;
		assertEquals(expected, hash);
	}

	@Test(enabled = false)
	public void booleanHashes() {
		boolean value = false;
		int expected = 17 * 31;

		int hash = HashUtil.startHash(value);
		assertEquals(expected, hash);

		value = true;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 1;
		assertEquals(expected, hash);

		value = true;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 1;
		assertEquals(expected, hash);

		value = false;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 0;
		assertEquals(expected, hash);

		value = false;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 0;
		assertEquals(expected, hash);
	}

	@Test(enabled = false)
	public void byteHashes() {
		byte value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.startHash(value);
		assertEquals(expected, hash);

		value = (byte) 254;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 2;
		assertEquals(expected, hash);

		value = (byte) 127;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 127;
		assertEquals(expected, hash);

		value = (byte) 128;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 128;
		assertEquals(expected, hash);

		for (int i = 1; i < 256; i++) {
			value = (byte) i;
			hash = HashUtil.nextHash(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}

	@Test(enabled = false)
	public void shortHashes() {
		short value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.startHash(value);
		assertEquals(expected, hash);

		value = (short) 65534;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 2;
		assertEquals(expected, hash);

		value = (short) 32767;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 32767;
		assertEquals(expected, hash);

		value = (short) 32768;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 32768;
		assertEquals(expected, hash);

		for (int i = 1; i < 65536; i++) {
			value = (short) i;
			hash = HashUtil.nextHash(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}

	@Test(enabled = false)
	public void charHashes() {
		char value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.startHash(value);
		assertEquals(expected, hash);

		value = (char) 65534;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 65534;
		assertEquals(expected, hash);

		value = (char) 32767;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 32767;
		assertEquals(expected, hash);

		value = (char) 32768;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 32768;
		assertEquals(expected, hash);

		for (int i = 1; i < 65536; i++) {
			value = (char) i;
			hash = HashUtil.nextHash(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}

	@Test(enabled = false)
	public void intHashes() {
		int value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.startHash(value);
		assertEquals(expected, hash);

		value = (int) 4294967295L; // 2^32-1
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 1;
		assertEquals(expected, hash);

		value = (int) 2147483647L; // 2^31-1
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 2147483647;
		assertEquals(expected, hash);

		value = (int) 2147483648L; // 2^31
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + -2147483648;
		assertEquals(expected, hash);
	}

	@Test(enabled = false)
	public void longHashes() {
		long value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.startHash(value);
		assertEquals(expected, hash);

		value = 9223372036854775807L; // 2^63-1
		hash = HashUtil.nextHash(hash, value);
		int masked = (int) (value >>> 32 ^ value);
		expected = expected * 31 + masked;
		assertEquals(expected, hash);

		value++; // 2^31
		hash = HashUtil.nextHash(hash, value);
		masked = (int) (value >>> 32 ^ value);
		expected = expected * 31 + masked;
		assertEquals(expected, hash);
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
				for (short c = 9; c <= 900; c += 9) {
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
		// less then 1.5%
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
		// System.out.printf("t0=%d, t1=%d, t2=%d,t3=%d, collisions=%d/%d\n", t0, t1, t2, t3, colls, n);
	}

	private static final int hash(long a, int b, short c, byte d) {
		int h = HashUtil.startHash(a);
		h = HashUtil.nextHash(h, b);
		h = HashUtil.nextHash(h, c);
		h = HashUtil.nextHash(h, d);
		return h;
	}
}
