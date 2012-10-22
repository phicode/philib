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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;

import org.testng.annotations.Test;

public class HashUtilTest {

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
	public void simpleShort() {
		for (short a = -5000; a < 5000; a++) {
			for (short b = -5000; b < 5000; b++) {
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
		for (int a = 0; a < 10000; a++) {
			for (int b = 0; b < 10000; b++) {
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
		for (long a = 0; a < 10000; a++) {
			for (long b = 0; b < 10000; b++) {
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
	public void f() {
		// test other methods
		fail();
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
		// System.out.printf("t0=%d, t1=%d, t2=%d,t3=%d, collisions=%d/%d\n",
		// t0, t1, t2, t3, colls, n);
	}

	private static final int hash(long a, int b, short c, byte d) {
		int h = HashUtil.startHash(a);
		h = HashUtil.nextHash(h, b);
		h = HashUtil.nextHash(h, c);
		h = HashUtil.nextHash(h, d);
		return h;
	}
}
