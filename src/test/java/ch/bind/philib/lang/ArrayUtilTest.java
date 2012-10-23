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

import static ch.bind.philib.lang.ArrayUtil.EMPTY_BYTE_ARRAY;
import static ch.bind.philib.lang.ArrayUtil.append;
import static ch.bind.philib.lang.ArrayUtil.concat;
import static ch.bind.philib.lang.ArrayUtil.extractBack;
import static ch.bind.philib.lang.ArrayUtil.extractFront;
import static ch.bind.philib.lang.ArrayUtil.formatShortHex;
import static ch.bind.philib.lang.ArrayUtil.memsetZero;
import static ch.bind.philib.lang.ArrayUtil.pickRandom;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import org.testng.annotations.Test;

public class ArrayUtilTest {

	@Test(expectedExceptions = NullPointerException.class)
	public void sourceNull() {
		Object[] arr = new Object[1];

		pickRandom(null, arr);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void destinationNull() {
		Object[] arr = new Object[1];
		pickRandom(arr, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void sourceSmallerThenDestination() {
		Object[] src = new Object[1];
		Object[] dst = new Object[2];
		pickRandom(src, dst);
	}

	@Test
	public void equalSize() {
		final int N = 4096;
		Integer[] src = new Integer[N];
		Integer[] dst = new Integer[N];
		boolean[] found = new boolean[N];
		for (int i = 0; i < N; i++) {
			src[i] = i;
		}
		pickRandom(src, dst);
		for (int i = 0; i < N; i++) {
			int v = dst[i].intValue();
			assertTrue(v >= 0);
			assertTrue(v < N);
			assertFalse(found[v]);
			found[v] = true;
		}
	}

	@Test
	public void concatNullNull() {
		byte[] r = concat(null, null);
		assertNotNull(r);
		assertEquals(0, r.length);
	}

	@Test
	public void concatNullEmpty() {
		byte[] r = concat(null, EMPTY_BYTE_ARRAY);
		assertNotNull(r);
		assertEquals(0, r.length);
	}

	@Test
	public void concatEmptyNull() {
		byte[] r = concat(EMPTY_BYTE_ARRAY, null);
		assertNotNull(r);
		assertEquals(0, r.length);
	}

	@Test
	public void concatEmptyEmpty() {
		byte[] r = concat(EMPTY_BYTE_ARRAY, EMPTY_BYTE_ARRAY);
		assertNotNull(r);
		assertEquals(0, r.length);
	}

	@Test
	public void concatNormalNull() {
		byte[] a = "123".getBytes();
		byte[] b = null;
		byte[] c = concat(a, b);
		assertNotNull(c);
		assertEquals(3, c.length);
		assertTrue(Arrays.equals(a, c));
	}

	@Test
	public void concatNullNormal() {
		byte[] a = null;
		byte[] b = "123".getBytes();
		byte[] c = concat(a, b);
		assertNotNull(c);
		assertEquals(3, c.length);
		assertTrue(Arrays.equals(b, c));
	}

	@Test
	public void concatNormalNormal() {
		byte[] a = "123".getBytes();
		byte[] b = "abc".getBytes();
		byte[] c = concat(a, b);
		byte[] ce = "123abc".getBytes();
		assertNotNull(c);
		assertEquals(6, c.length);
		assertTrue(Arrays.equals(ce, c));
	}

	@Test
	public void appendEnoughCap() {
		byte[] a = "123".getBytes();
		byte[] b = "abc".getBytes();
		byte[] c = append(a, b, 8);
		byte[] ce = "123abc".getBytes();
		assertNotNull(c);
		assertEquals(6, c.length);
		assertTrue(Arrays.equals(ce, c));
	}

	@Test
	public void appendCapped() {
		byte[] a = "123".getBytes();
		byte[] b = "abc".getBytes();

		byte[] c = append(a, b, 5);
		assertNotNull(c);
		assertEquals(5, c.length);
		assertTrue(Arrays.equals("123ab".getBytes(), c));

		c = append(a, b, 4);
		assertNotNull(c);
		assertEquals(4, c.length);
		assertTrue(Arrays.equals("123a".getBytes(), c));

		c = append(a, b, 3);
		assertNotNull(c);
		assertEquals(3, c.length);
		assertTrue(Arrays.equals("123".getBytes(), c));

		c = append(a, b, 2);
		assertNotNull(c);
		assertEquals(2, c.length);
		assertTrue(Arrays.equals("12".getBytes(), c));

		c = append(a, b, 1);
		assertNotNull(c);
		assertEquals(1, c.length);
		assertTrue(Arrays.equals("1".getBytes(), c));

		c = append(a, b, 0);
		assertNotNull(c);
		assertEquals(0, c.length);

		c = append(a, b, -1);
		assertNotNull(c);
		assertEquals(0, c.length);
	}

	@Test
	public void appendNull() {
		byte[] a = "123".getBytes();
		byte[] b = "abc".getBytes();

		byte[] c = append(null, b, 4);
		assertNotNull(c);
		assertEquals(3, c.length);
		assertTrue(Arrays.equals(b, c));

		c = append(a, null, 4);
		assertNotNull(c);
		assertEquals(3, c.length);
		assertTrue(Arrays.equals(a, c));
	}

	@Test
	public void testExtractBack() {
		byte[] a = "abcd".getBytes();
		assertEquals(extractBack(a, 0), EMPTY_BYTE_ARRAY);
		assertEquals(extractBack(a, 1), "d".getBytes());
		assertEquals(extractBack(a, 2), "cd".getBytes());
		assertEquals(extractBack(a, 3), "bcd".getBytes());
		assertEquals(extractBack(a, 4), "abcd".getBytes());
	}

	@Test
	public void testExtractFront() {
		byte[] a = "abcd".getBytes();
		assertEquals(extractFront(a, 0), EMPTY_BYTE_ARRAY);
		assertEquals(extractFront(a, 1), "a".getBytes());
		assertEquals(extractFront(a, 2), "ab".getBytes());
		assertEquals(extractFront(a, 3), "abc".getBytes());
		assertEquals(extractFront(a, 4), "abcd".getBytes());
	}

	@Test
	public void testFormatShortHexArray() {
		assertEquals(formatShortHex((byte[]) null), "");
		assertEquals(formatShortHex(EMPTY_BYTE_ARRAY), "");

		byte[] a = "abcd".getBytes();
		assertEquals(formatShortHex(a), "61626364");
	}

	@Test
	public void testFormatShortHexArrayExtended() {
		assertEquals(formatShortHex((byte[]) null, 0, 1), "");
		assertEquals(formatShortHex(EMPTY_BYTE_ARRAY, 1, 2), "");

		byte[] a = "abcd".getBytes();
		assertEquals(formatShortHex(a, 1, 2), "6263");
		assertEquals(formatShortHex(a, 1, 5555), "626364");
	}

	@Test
	public void testFormatShortHexByteBuffer() {
		assertEquals(formatShortHex((ByteBuffer) null), "");
		assertEquals(formatShortHex(ByteBuffer.wrap(EMPTY_BYTE_ARRAY)), "");

		byte[] a = "\u0002abcd".getBytes();
		ByteBuffer arrayBb = ByteBuffer.wrap(a);
		assertEquals(arrayBb.remaining(), 5);
		assertEquals(formatShortHex(arrayBb), "0261626364");
		assertEquals(arrayBb.remaining(), 5);

		ByteBuffer directBb = ByteBuffer.allocateDirect(5);
		directBb.put(a);
		directBb.clear();
		assertEquals(directBb.remaining(), 5);
		assertEquals(formatShortHex(directBb), "0261626364");
		assertEquals(directBb.remaining(), 5);
	}

	@Test
	public void dontCareAboutNulls() {
		memsetZero((byte[]) null);
		memsetZero((ByteBuffer) null);
	}

	@Test
	public void memsetArray() {
		Random rand = new Random();
		for (int i = 0; i < 9999; i++) {
			byte[] b = new byte[i];
			rand.nextBytes(b);
			memsetZero(b);
			for (int j = 0; j < i; j++) {
				assertTrue(b[j] == 0);
			}
		}
	}

	@Test
	public void memsetArrayByteBuffer() {
		Random rand = new Random();
		for (int i = 0; i < 9999; i++) {
			byte[] b = new byte[i];
			rand.nextBytes(b);
			ByteBuffer bb = ByteBuffer.wrap(b);
			memsetZero(bb);
			for (int j = 0; j < i; j++) {
				assertTrue(b[j] == 0);
			}
		}
	}

	@Test
	public void memsetDirectByteBuffer() {
		Random rand = new Random();
		for (int i = 0; i < 9999; i++) {
			byte[] b = new byte[i];
			rand.nextBytes(b);
			ByteBuffer bb = ByteBuffer.allocateDirect(i);
			bb.put(b);
			bb.clear();
			memsetZero(bb);
			bb.get(b);
			for (int j = 0; j < i; j++) {
				assertTrue(b[j] == 0);
			}
		}
	}
}
