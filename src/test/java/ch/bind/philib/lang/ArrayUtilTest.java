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

import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static ch.bind.philib.lang.ArrayUtil.EMPTY_BYTE_ARRAY;
import static ch.bind.philib.lang.ArrayUtil.append;
import static ch.bind.philib.lang.ArrayUtil.concat;
import static ch.bind.philib.lang.ArrayUtil.contains;
import static ch.bind.philib.lang.ArrayUtil.extractBack;
import static ch.bind.philib.lang.ArrayUtil.extractFront;
import static ch.bind.philib.lang.ArrayUtil.find;
import static ch.bind.philib.lang.ArrayUtil.formatShortHex;
import static ch.bind.philib.lang.ArrayUtil.memclr;
import static ch.bind.philib.lang.ArrayUtil.pickRandom;
import static ch.bind.philib.lang.ArrayUtil.prepend;
import static ch.bind.philib.lang.ArrayUtil.remove;
import static ch.bind.philib.lang.ArrayUtil.toArray;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

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
	public void sourceSmallerThanDestination() {
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
			int v = dst[i];
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
	public void limitedFormatShortHex() {
		byte[] abcd = "abcd".getBytes();
		ByteBuffer bb = ByteBuffer.wrap(abcd);

		String a1 = formatShortHex(bb, 8);
		String b1 = formatShortHex(bb, 2);
		String c1 = formatShortHex((ByteBuffer) bb.position(1), 2);
		String d1 = formatShortHex((ByteBuffer) bb.position(1), 8);

		String a2 = formatShortHex(abcd, 0, 8);
		String b2 = formatShortHex(abcd, 0, 2);
		String c2 = formatShortHex(abcd, 1, 2);
		String d2 = formatShortHex(abcd, 1, 8);

		assertEquals(a1, "61626364");
		assertEquals(a2, "61626364");
		assertEquals(b1, "6162");
		assertEquals(b2, "6162");
		assertEquals(c1, "6263");
		assertEquals(c2, "6263");
		assertEquals(d1, "626364");
		assertEquals(d2, "626364");
	}

	@Test
	public void dontCareAboutNulls() {
		memclr((byte[]) null);
		memclr((ByteBuffer) null);
	}

	@Test
	public void memsetArray() {
		Random rand = ThreadLocalRandom.current();
		for (int i = 0; i < 2000; i++) {
			byte[] b = new byte[i];
			rand.nextBytes(b);
			memclr(b);
			for (int j = 0; j < i; j++) {
				assertTrue(b[j] == 0);
			}
		}
	}

	@Test
	public void memsetArrayByteBuffer() {
		Random rand = ThreadLocalRandom.current();
		for (int i = 0; i < 2000; i++) {
			byte[] b = new byte[i];
			rand.nextBytes(b);
			ByteBuffer bb = ByteBuffer.wrap(b);
			memclr(bb);
			for (int j = 0; j < i; j++) {
				assertTrue(b[j] == 0);
			}
		}
	}

	@Test
	public void memsetDirectByteBuffer() {
		Random rand = ThreadLocalRandom.current();
		for (int i = 0; i < 2000; i++) {
			byte[] b = new byte[i];
			rand.nextBytes(b);
			ByteBuffer bb = ByteBuffer.allocateDirect(i);
			bb.put(b);
			bb.clear();
			memclr(bb);
			bb.get(b);
			for (int j = 0; j < i; j++) {
				assertTrue(b[j] == 0);
			}
		}
	}

	@Test
	public void findAndContains() {
		byte[] abc = "abc".getBytes();
		byte[] xyz = "xyz".getBytes();
		byte[] abcx = "abcx".getBytes();
		byte[] xabc = "xabc".getBytes();

		byte[] e = EMPTY_BYTE_ARRAY;

		// null tests
		assertEquals(find(null, null), -1);
		assertEquals(find(null, abc), -1);
		assertEquals(find(abc, null), -1);
		assertFalse(contains(null, null));
		assertFalse(contains(null, abc));
		assertFalse(contains(abc, null));

		// zero length tests
		assertEquals(find(e, e), -1);
		assertEquals(find(e, abc), -1);
		assertEquals(find(abc, e), -1);
		assertFalse(contains(e, e));
		assertFalse(contains(e, abc));
		assertFalse(contains(abc, e));

		// equal length, equal content
		assertEquals(find(abc, abc), 0);
		assertTrue(contains(abc, abc));

		// equal length, not equal content
		assertEquals(find(abc, xyz), -1);
		assertEquals(find(xyz, abc), -1);
		assertFalse(contains(abc, xyz));
		assertFalse(contains(xyz, abc));

		// search longer than data
		assertEquals(find(abc, xabc), -1);
		assertFalse(contains(abc, xabc));

		// different length, search ok
		assertEquals(find(xabc, abc), 1);
		assertEquals(find(abcx, abc), 0);
		assertTrue(contains(xabc, abc));
		assertTrue(contains(abcx, abc));
	}

	@Test
	public void findMany() {
		byte[] a = "abaabaaabaaaabaaaaab".getBytes();
		byte[] _1 = "ab".getBytes();
		byte[] _2 = "aab".getBytes();
		byte[] _3 = "aaab".getBytes();
		byte[] _4 = "aaaab".getBytes();
		byte[] _5 = "aaaaab".getBytes();
		byte[] _6 = "aaaaaab".getBytes();

		assertTrue(contains(a, _1));
		assertTrue(contains(a, _2));
		assertTrue(contains(a, _3));
		assertTrue(contains(a, _4));
		assertTrue(contains(a, _5));
		assertFalse(contains(a, _6));

		assertEquals(find(a, _1), 0);
		assertEquals(find(a, _2), 2); // +2
		assertEquals(find(a, _3), 5); // +3
		assertEquals(find(a, _4), 9); // +4
		assertEquals(find(a, _5), 14); // +5
		assertEquals(find(a, _6), -1);

		assertEquals(find(a, _1, 0), 0); // ->ab

		// aba->ab
		assertEquals(find(a, _1, 1), 3);
		assertEquals(find(a, _1, 2), 3);
		assertEquals(find(a, _1, 3), 3);

		// abaabaa->ab
		assertEquals(find(a, _1, 4), 7);
		assertEquals(find(a, _1, 5), 7);
		assertEquals(find(a, _1, 6), 7);
		assertEquals(find(a, _1, 7), 7);

		// abaabaaabaaa->ab
		assertEquals(find(a, _1, 8), 12);
		assertEquals(find(a, _1, 9), 12);
		assertEquals(find(a, _1, 10), 12);
		assertEquals(find(a, _1, 11), 12);
		assertEquals(find(a, _1, 12), 12);

		// abaabaaabaaaabaaaa->ab
		assertEquals(find(a, _1, 13), 18);
		assertEquals(find(a, _1, 14), 18);
		assertEquals(find(a, _1, 15), 18);
		assertEquals(find(a, _1, 16), 18);
		assertEquals(find(a, _1, 17), 18);
		assertEquals(find(a, _1, 18), 18);

		assertEquals(find(a, _1, 19), -1);
	}

	@Test
	public void appendNullX() {
		String x = "foo";
		String[] xs = null;
		String[] res = append(String.class, xs, x);
		assertNotNull(res);
		assertEquals(res.getClass(), String[].class);
		assertEquals(res.length, 1);
		assertSame(res[0], x);
	}

	@Test
	public void prependNullX() {
		String x = "foo";
		String[] xs = null;
		String[] res = prepend(String.class, xs, x);
		assertNotNull(res);
		assertEquals(res.getClass(), String[].class);
		assertEquals(res.length, 1);
		assertSame(res[0], x);
	}

	@Test
	public void appendNullXOtherType() {
		String x = "foo";
		String[] xs = null;
		Object[] res = append(Object.class, xs, x);
		assertNotNull(res);
		assertEquals(res.getClass(), Object[].class);
		assertEquals(res.length, 1);
		assertSame(res[0], x);
	}

	@Test
	public void prependNullXOtherType() {
		String x = "foo";
		String[] xs = null;
		Object[] res = prepend(Object.class, xs, x);
		assertNotNull(res);
		assertEquals(res.getClass(), Object[].class);
		assertEquals(res.length, 1);
		assertSame(res[0], x);
	}

	@Test
	public void appendXY() {
		String x = "foo";
		String[] xs = {"bar"};
		String[] res = append(String.class, xs, x);
		assertNotNull(res);
		assertEquals(res.getClass(), String[].class);
		assertEquals(res.length, 2);
		assertSame(res[0], xs[0]);
		assertSame(res[1], x);
	}

	@Test
	public void prependXY() {
		String x = "foo";
		String[] xs = {"bar"};
		String[] res = prepend(String.class, xs, x);
		assertNotNull(res);
		assertEquals(res.getClass(), String[].class);
		assertEquals(res.length, 2);
		assertSame(res[0], x);
		assertSame(res[1], xs[0]);
	}

	@Test
	public void appendXYOtherType() {
		String x = "foo";
		String[] xs = {"bar"};
		Object[] res = append(Object.class, xs, x);
		assertNotNull(res);
		assertEquals(res.getClass(), Object[].class);
		assertEquals(res.length, 2);
		assertTrue(res[0] == xs[0]);
		assertTrue(res[1] == x);
	}

	@Test
	public void prependXYOtherType() {
		String x = "foo";
		String[] xs = {"bar"};
		Object[] res = prepend(Object.class, xs, x);
		assertNotNull(res);
		assertEquals(res.getClass(), Object[].class);
		assertEquals(res.length, 2);
		assertTrue(res[0] == x);
		assertTrue(res[1] == xs[0]);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void appendNoNullClazz() {
		String[] xs = {"bar"};
		append((Class<String>) null, xs, "quack");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void prependNoNullClazz() {
		String[] xs = {"bar"};
		prepend((Class<String>) null, xs, "quack");
	}

	@Test
	public void appendNullValue() {
		String[] xs = {"bar"};
		String[] exp = {"bar", null};
		assertEquals(append(String.class, xs, null), exp);
	}

	@Test
	public void prependNullValue() {
		String[] xs = {"bar"};
		String[] exp = {null, "bar"};
		assertEquals(prepend(String.class, xs, null), exp);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void removeNoNullClazz() {
		String[] from = {"bar"};
		remove((Class<String>) null, from, "foo");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void removeNoNullFrom() {
		String[] from = null;
		remove(String.class, from, "foo");
	}

	@Test
	public void removeNone() {
		String[] from = {"a", "b", "c"};
		String[] res = remove(String.class, from, "d");
		assertTrue(from == res);
	}

	@Test
	public void removeFront() {
		String[] from = {"a", "b", "c"};
		String[] res = remove(String.class, from, "a");
		assertEquals(res.length, 2);
		assertEquals(res[0], "b");
		assertEquals(res[1], "c");
	}

	@Test
	public void removeMid() {
		String[] from = {"a", "b", "c"};
		String[] res = remove(String.class, from, "b");
		assertEquals(res.length, 2);
		assertEquals(res[0], "a");
		assertEquals(res[1], "c");
	}

	@Test
	public void removeBack() {
		String[] from = {"a", "b", "c"};
		String[] res = remove(String.class, from, "c");
		assertEquals(res.length, 2);
		assertEquals(res[0], "a");
		assertEquals(res[1], "b");
	}

	@Test
	public void removeMultiple() {
		String[] from = {"a", "b", "c", "c"};
		String[] res = remove(String.class, from, "c");
		assertEquals(res.length, 2);
		assertEquals(res[0], "a");
		assertEquals(res[1], "b");
	}

	@Test
	public void removeAll() {
		String[] from = {"c", "c", "c", "c"};
		String[] res = remove(String.class, from, "c");
		assertEquals(res.length, 0);
	}

	@Test
	public void toArrayNull() {
		Integer[] x = toArray(Integer.class, null);
		assertNotNull(x);
		assertTrue(x.getClass() == Integer[].class);
		assertEquals((x).length, 0);
	}

	@Test
	public void toArrayEmpty() {
		List<String> l = new ArrayList<String>();
		Object[] x = toArray(Object.class, l);
		assertNotNull(x);
		assertTrue(x.getClass() == Object[].class);
		assertEquals((x).length, 0);
	}

	@Test
	public void toArrayNormal() {
		List<String> l = Arrays.asList("a", "b");
		Object x = toArray(String.class, l);
		assertNotNull(x);
		assertTrue(x.getClass() == String[].class);
		assertEquals(((String[]) x).length, 2);
		assertEquals(((String[]) x)[0], "a");
		assertEquals(((String[]) x)[1], "b");
	}
}
