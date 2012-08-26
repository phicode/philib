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

import org.testng.annotations.Test;

public class HashUtilTest {

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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
	public void tloatHashes() {
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

	// @Test
	// public void distribution() {
	// // int32, int32, int64, int64
	// int matrixSide = 128;
	// int matrixSize = matrixSide * matrixSide;
	// short matrix[] = new short[matrixSize];
	// byte[] data = new byte[16];
	// Random r = new Random();
	// for (int i = 0; i < matrixSize * 5; i++) {
	// r.nextBytes(data);
	// int h = MurmurHash.murmur3(data);
	// int b = Math.abs(h) % matrixSize;
	// matrix[b]++;
	// }
	// int lastY = matrixSide - 1;
	// for (int i = matrixSize - 1; i >= 0; i--) {
	// int y = i / matrixSide;
	// int v = matrix[i];
	// if (v == 0) {
	// System.out.print(' ');
	// } else if (v > 9) {
	// System.out.print('#');
	// } else {
	// System.out.print(v);
	// }
	// if (y != lastY) {
	// System.out.println();
	// lastY = y;
	// }
	// }
	// }
}
