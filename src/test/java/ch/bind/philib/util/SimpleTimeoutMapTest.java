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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Map.Entry;

import org.testng.annotations.Test;

public class SimpleTimeoutMapTest {

	@Test
	public void putEntry() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.put(0, 123, 456);
		assertEquals(1, map.size());
		assertFalse(map.isEmpty());
		map.remove(123);
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	public void duplicateKeys() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		int key = 12345678;
		Integer prev1 = map.put(1000, key, 1);
		Integer prev2 = map.put(1000, key, 1234);
		assertNull(prev1);
		assertTrue(prev2 != null && prev2.intValue() == 1);
		assertEquals(map.get(key).intValue(), 1234);
		long timeToNextTimeout = map.getTimeToNextTimeout();
		assertTrue(timeToNextTimeout > 800 && timeToNextTimeout <= 1000);
	}

	@Test
	public void nullOnEmpty() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		assertTrue(map.isEmpty());
		assertNull(map.pollTimeout());
		assertNull(map.get(1));
	}

	@Test
	public void returnNullOnNoTimeouts() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.put(1, 123, 456);
		assertFalse(map.isEmpty());
		assertNull(map.pollTimeout());
	}

	@Test
	public void returnZeroOnImmediateTimeout() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.putWithTimestamp(0, 123, 456);
		assertEquals(map.getTimeToNextTimeout(), 0);
		assertNotNull(map.pollTimeout());
		assertTrue(map.isEmpty());
	}

	@Test
	public void longMaxValueOnEmpty() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		assertEquals(map.getTimeToNextTimeout(), Long.MAX_VALUE);
	}

	@Test
	public void removeOnEmptyReturnsNull() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.put(1000, 123, 456);
		assertEquals(map.remove(123).intValue(), 456);
		assertNull(map.remove(123));
	}

	@Test
	public void clear() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.put(1, 123, 456);
		map.clear();
		assertEquals(map.size(), 0);
		assertTrue(map.isEmpty());
		assertFalse(map.containsKey(123));
		assertNull(map.remove(123));
	}

	@Test
	public void sameTimestamp() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		long timestamp = 123000;
		for (int i = 0; i < 1000; i++) {
			Integer x = Integer.valueOf(i);
			map.putWithTimestamp(timestamp, x, x);
		}
		int[] keys = new int[1000];
		for (int i = 0; i < 1000; i++) {
			Entry<Integer, Integer> e = map.pollTimeout();
			assertNotNull(e);
			assertEquals(e.getKey(), e.getValue());
			keys[i] = e.getKey().intValue();
		}
		assertTrue(map.isEmpty());
		// the first key should be on the exact timestamp, the others
		// distributed randomly
		assertEquals(keys[0], 0);

		int last = 0;
		int numSorted = 0;
		for (int i = 1; i < 1000; i++) {
			int now = keys[i];
			if (now == (last + 1)) {
				numSorted++;
			}
			last = now;
		}
		assertTrue(numSorted < 10);
	}

	@Test
	public void entryEqualsAndHashcode() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();

		map.putWithTimestamp(0, 111, 222);
		Entry<Integer, Integer> a1 = map.pollTimeout();

		map.putWithTimestamp(0, 111, 222);
		Entry<Integer, Integer> a2 = map.pollTimeout();

		map.putWithTimestamp(0, 111, 111);
		Entry<Integer, Integer> b = map.pollTimeout();

		map.putWithTimestamp(0, 222, 222);
		Entry<Integer, Integer> c = map.pollTimeout();

		assertNotNull(a1);
		assertNotNull(a2);
		assertNotNull(b);
		assertNotNull(c);

		assertEquals(a1.hashCode(), a2.hashCode());
		assertTrue(a1.equals(a1));
		assertTrue(a1.equals(a2));
		assertFalse(a1.equals(b));
		assertFalse(a1.equals(c));
		assertFalse(a1.equals("abc"));
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void entrySetValueUnsupported() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.putWithTimestamp(1, 123, 456);
		Entry<Integer, Integer> e = map.pollTimeout();
		e.setValue(123);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "timeout must not be negative")
	public void addNoNegativeTimeout() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.put(-1, 1, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "timestamp must not be negative")
	public void addNoNegativeTimestamp() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.putWithTimestamp(-1, 1, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "key must not be null")
	public void noNullKey1() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.put(0, null, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "key must not be null")
	public void noNullKey2() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.putWithTimestamp(0, null, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "value must not be null")
	public void noNullValue1() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.put(0, 1, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "value must not be null")
	public void noNullValue2() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<Integer, Integer>();
		map.putWithTimestamp(0, 1, null);
	}
}
