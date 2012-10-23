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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class CompareUtilTest {

	@Test
	public void equalityObjObj() {
		Object a = "a";
		Object b = "b";

		boolean eq = CompareUtil.equals(a, b);
		assertFalse(eq);

		a = "b";
		eq = CompareUtil.equals(a, b);
		assertTrue(eq);

		b = "a";
		eq = CompareUtil.equals(a, b);
		assertFalse(eq);
	}

	@Test
	public void equalityNullNull() {
		Object a = null;
		Object b = null;

		boolean eq = CompareUtil.equals(a, b);
		assertTrue(eq);
	}

	@Test
	public void equalityObjNull() {
		Object a = "a";
		Object b = null;

		boolean eq = CompareUtil.equals(a, b);
		assertFalse(eq);
	}

	@Test
	public void equalityNullObj() {
		Object a = null;
		Object b = "b";

		boolean eq = CompareUtil.equals(a, b);
		assertFalse(eq);
	}

	@Test
	public void compareStringString() {
		String a = "a";
		String b = "b";

		int cmp = CompareUtil.compare(a, b);
		assertEquals(-1, cmp);

		a = "b";
		cmp = CompareUtil.compare(a, b);
		assertEquals(0, cmp);

		b = "a";
		cmp = CompareUtil.compare(a, b);
		assertEquals(1, cmp);
	}

	@Test
	public void compareStringNull() {
		String a = "a";
		String b = null;

		int cmp = CompareUtil.compare(a, b);
		assertEquals(1, cmp); // a > b
	}

	@Test
	public void compareNullString() {
		String a = null;
		String b = "b";

		int cmp = CompareUtil.compare(a, b);
		assertEquals(-1, cmp); // a < b
	}

	@Test
	public void compareNullNull() {
		String a = null;
		String b = null;

		int cmp = CompareUtil.compare(a, b);
		assertEquals(0, cmp); // a == b
	}

	@Test
	public void normalizeInt() {
		assertEquals(0, CompareUtil.normalize(0));
		for (int i = -1000; i < 0; i++) {
			assertEquals(-1, CompareUtil.normalize(i));
		}
		for (int i = 1; i <= 1000; i++) {
			assertEquals(1, CompareUtil.normalize(i));
		}
		assertEquals(-1, CompareUtil.normalize(Integer.MIN_VALUE));
		assertEquals(1, CompareUtil.normalize(Integer.MAX_VALUE));
	}

	@Test
	public void normalizeLong() {
		assertEquals(0L, CompareUtil.normalize(0L));
		for (long i = -1000; i < 0; i++) {
			assertEquals(-1L, CompareUtil.normalize(i));
		}
		for (long i = 1; i <= 1000; i++) {
			assertEquals(1L, CompareUtil.normalize(i));
		}
		assertEquals(-1L, CompareUtil.normalize(Long.MIN_VALUE));
		assertEquals(1L, CompareUtil.normalize(Long.MAX_VALUE));
	}

	@Test
	public void diffBool() {
		assertEquals(CompareUtil.diff(true, true), 0);
		assertEquals(CompareUtil.diff(true, false), 1);
		assertEquals(CompareUtil.diff(false, true), -1);
		assertEquals(CompareUtil.diff(false, false), 0);
	}

	@Test
	public void diffByte() {
		for (int a = 0; a < 255; a++) {
			byte x = (byte) a;
			for (int b = 0; b < 255; b++) {
				byte y = (byte) b;
				if (a == b) {
					assertEquals(CompareUtil.diff(x, y), 0);
				} else if (a < b) {
					assertEquals(CompareUtil.diff(x, y), -1);
				} else {
					assertEquals(CompareUtil.diff(x, y), 1);
				}
			}
		}
	}

	@Test
	public void diffShort() {
		assertEquals(CompareUtil.diff((short) 1, (short) 1), 0);
		assertEquals(CompareUtil.diff((short) 5, (short) 1), 1);
		assertEquals(CompareUtil.diff((short) 1, (short) 5), -1);

		assertEquals(CompareUtil.diff(Short.MIN_VALUE, Short.MIN_VALUE), 0);
		assertEquals(CompareUtil.diff(Short.MIN_VALUE + 2, Short.MIN_VALUE), 1);
		assertEquals(CompareUtil.diff(Short.MIN_VALUE, Short.MIN_VALUE + 2), -1);

		assertEquals(CompareUtil.diff(Short.MAX_VALUE, Short.MAX_VALUE), 0);
		assertEquals(CompareUtil.diff(Short.MAX_VALUE, Short.MAX_VALUE - 2), 1);
		assertEquals(CompareUtil.diff(Short.MAX_VALUE - 2, Short.MAX_VALUE), -1);
	}

	@Test
	public void diffChar() {
		assertEquals(CompareUtil.diff((char) 1, (char) 1), 0);
		assertEquals(CompareUtil.diff((char) 5, (char) 1), 1);
		assertEquals(CompareUtil.diff((char) 1, (char) 5), -1);

		assertEquals(CompareUtil.diff(Character.MIN_VALUE, Character.MIN_VALUE), 0);
		assertEquals(CompareUtil.diff(Character.MIN_VALUE + 2, Character.MIN_VALUE), 1);
		assertEquals(CompareUtil.diff(Character.MIN_VALUE, Character.MIN_VALUE + 2), -1);

		assertEquals(CompareUtil.diff(Character.MAX_VALUE, Character.MAX_VALUE), 0);
		assertEquals(CompareUtil.diff(Character.MAX_VALUE, Character.MAX_VALUE - 2), 1);
		assertEquals(CompareUtil.diff(Character.MAX_VALUE - 2, Character.MAX_VALUE), -1);
	}

	@Test
	public void diffInt() {
		assertEquals(CompareUtil.diff(1, 1), 0);
		assertEquals(CompareUtil.diff(5, 1), 1);
		assertEquals(CompareUtil.diff(1, 5), -1);

		assertEquals(CompareUtil.diff(Integer.MIN_VALUE, Integer.MIN_VALUE), 0);
		assertEquals(CompareUtil.diff(Integer.MIN_VALUE + 2, Integer.MIN_VALUE), 1);
		assertEquals(CompareUtil.diff(Integer.MIN_VALUE, Integer.MIN_VALUE + 2), -1);

		assertEquals(CompareUtil.diff(Integer.MAX_VALUE, Integer.MAX_VALUE), 0);
		assertEquals(CompareUtil.diff(Integer.MAX_VALUE, Integer.MAX_VALUE - 2), 1);
		assertEquals(CompareUtil.diff(Integer.MAX_VALUE - 2, Integer.MAX_VALUE), -1);
	}

	@Test
	public void diffLong() {
		assertEquals(CompareUtil.diff(1L, 1L), 0);
		assertEquals(CompareUtil.diff(5L, 1L), 1);
		assertEquals(CompareUtil.diff(1L, 5L), -1);

		assertEquals(CompareUtil.diff(Long.MIN_VALUE, Long.MIN_VALUE), 0);
		assertEquals(CompareUtil.diff(Long.MIN_VALUE + 2, Long.MIN_VALUE), 1);
		assertEquals(CompareUtil.diff(Long.MIN_VALUE, Long.MIN_VALUE + 2), -1);

		assertEquals(CompareUtil.diff(Long.MAX_VALUE, Long.MAX_VALUE), 0);
		assertEquals(CompareUtil.diff(Long.MAX_VALUE, Long.MAX_VALUE - 2), 1);
		assertEquals(CompareUtil.diff(Long.MAX_VALUE - 2, Long.MAX_VALUE), -1);
	}

	@Test
	public void diffFloat() {
		assertEquals(CompareUtil.diff(1f, 1f), 0);
		assertEquals(CompareUtil.diff(5f, 1f), 1);
		assertEquals(CompareUtil.diff(1f, 5f), -1);

		//		assertEquals(CompareUtil.diff(Float.NaN, Float.NaN), 0);
		assertEquals(CompareUtil.diff(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), 0);
		assertEquals(CompareUtil.diff(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), 0);

		assertEquals(CompareUtil.diff(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), -1);
		assertEquals(CompareUtil.diff(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), 1);
	}

	@Test
	public void diffDouble() {
		assertEquals(CompareUtil.diff((double) 1f, (double) 1f), 0);
		assertEquals(CompareUtil.diff((double) 5f, (double) 1f), 1);
		assertEquals(CompareUtil.diff((double) 1f, (double) 5f), -1);

		assertEquals(CompareUtil.diff(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), 0);
		assertEquals(CompareUtil.diff(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), 0);

		assertEquals(CompareUtil.diff(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), -1);
		assertEquals(CompareUtil.diff(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), 1);
	}
}
