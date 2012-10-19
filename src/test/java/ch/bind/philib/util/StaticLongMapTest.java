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

package ch.bind.philib.util;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.testng.annotations.Test;

public class StaticLongMapTest {

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "null or empty collection provided")
	public void noNullCreateCollection() {
		StaticLongMap.create((Set<TestDummy>) null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "null or empty array provided")
	public void noNullCreateArray() {
		StaticLongMap.create((TestDummy[]) null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "null or empty collection provided")
	public void noEmptyCreateCollection() {
		StaticLongMap.create(Collections.<TestDummy> emptyList());
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "null or empty array provided")
	public void noEmptyCreateArray() {
		StaticLongMap.create(new TestDummy[0]);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void noNullElementsInCreateArray() {
		TestDummy a = new TestDummy(1, "a");
		StaticLongMap.create(new TestDummy[] {
				a, null });
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void noNullElementsInCreateCollection() {
		TestDummy a = new TestDummy(1, "a");
		StaticLongMap.create(Arrays.asList(null, a));
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "duplicate key: 1")
	public void noDuplicateKeys() {
		TestDummy a = new TestDummy(1, "a");
		TestDummy b = new TestDummy(1, "b");
		StaticLongMap.create(Arrays.asList(a, b));
	}

	@Test
	public void oneElement() {
		TestDummy a = new TestDummy(1, "a");
		String other = "b";
		StaticLongMap<String> map = StaticLongMap.create(new TestDummy[] { a });
		assertEquals(map.size(), 1);
		for (int i = -1000; i < 1000; i++) {
			if (i == 1) {
				assertEquals(map.containsKey(i), true);
				assertEquals(map.get(i), "a");
				assertEquals(map.getOrElse(i, other), "a");
			} else {
				assertEquals(map.containsKey(i), false);
				assertNull(map.get(i));
				assertEquals(map.getOrElse(i, other), other);
			}
		}
	}

	@Test
	public void twoElements() {
		TestDummy a = new TestDummy(1, "a");
		TestDummy b = new TestDummy(2, "b");
		String other = "c";
		StaticLongMap<String> map = StaticLongMap.create(new TestDummy[] {
				a, b });
		assertEquals(map.size(), 2);
		for (int i = -1000; i < 1000; i++) {
			if (i == 1) {
				assertEquals(map.containsKey(i), true);
				assertEquals(map.get(i), "a");
				assertEquals(map.getOrElse(i, other), "a");
			} else if (i == 2) {
				assertEquals(map.containsKey(i), true);
				assertEquals(map.get(i), "b");
				assertEquals(map.getOrElse(i, other), "b");
			} else {
				assertEquals(map.containsKey(i), false);
				assertNull(map.get(i));
				assertEquals(map.getOrElse(i, other), other);
			}
		}
	}

	@Test
	public void getOrThrow() {
		Set<TestDummy> xs = new HashSet<TestDummy>();
		for (int i = 0; i < 1000; i++) {
			int key = (i + 1) * 5;
			xs.add(new TestDummy(key, String.valueOf(key)));
		}
		String other = "b";
		StaticLongMap<String> map = StaticLongMap.create(xs);
		assertEquals(map.size(), 1000);
		for (int i = -1000; i < 10000; i++) {
			if (i > 0 && i % 5 == 0 && i <= 5000) {
				String expVal = String.valueOf(i);
				assertEquals(map.containsKey(i), true);
				assertEquals(map.get(i), expVal);
				assertEquals(map.getOrElse(i, other), expVal);
				assertEquals(map.getOrThrow(i), expVal);
			} else {
				assertEquals(map.containsKey(i), false);
				assertNull(map.get(i));
				assertEquals(map.getOrElse(i, other), other);
				try {
					map.getOrThrow(i);
					fail("should have thrown");
				} catch (Exception e) {
					assertTrue(e instanceof NoSuchElementException);
					assertEquals(e.getMessage(), "no value found for key: " + i);
				}
			}
		}
	}

	private static final class TestDummy implements LongPair<String> {

		private final long key;

		private final String value;

		TestDummy(long key, String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public long getKey() {
			return key;
		}

		@Override
		public String getValue() {
			return value;
		}
	}
}
