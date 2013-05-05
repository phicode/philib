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

package ch.bind.philib.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import ch.bind.philib.lang.Cloner;

@Test
public class LruCacheTest extends CacheTestBase {

	@Override
	<K, V> Cache<K, V> create() {
		return new LruCache<K, V>();
	}

	@Override
	<K, V> Cache<K, V> create(int capacity) {
		return new LruCache<K, V>(capacity);
	}

	@Override
	<K, V> Cache<K, V> create(Cloner<V> valueCloner) {
		return new LruCache<K, V>(valueCloner);
	}

	@Override
	int getMinCapacity() {
		return 1;
	}

	@Override
	int getDefaultCapacity() {
		return Cache.DEFAULT_CAPACITY;
	}

	@Override
	int getBucketSize() {
		return 1;
	}

	@Test
	public void fullCacheWhereOldObjectGetRemoved() {
		final int testSize = Cache.DEFAULT_CAPACITY;

		LruCache<String, String> cache = new LruCache<String, String>(testSize);

		for (int i = 1; i <= testSize; i++) {
			cache.set(itos(i), itos(i * i * i));
		}

		// the key 1, value 1 falls away
		cache.set("-1", "-1");

		for (int i = 2; i <= testSize; i++) {
			String v = cache.get(itos(i));
			assertEquals(itos(i * i * i), v);
		}

		assertEquals("-1", cache.get("-1"));

		// the key 2, value 8 falls away
		cache.set("-2", "-2");

		for (int i = 3; i <= testSize; i++) {
			String v = cache.get(itos(i));
			assertEquals(itos(i * i * i), v);
		}

		assertEquals(cache.get("-1"), "-1");
		assertEquals(cache.get("-2"), "-2");
	}

	@Test
	public void fullCacheWhereOldObjectGetRemoved2() {
		final int testSize = 10000;
		LruCache<String, String> cache = new LruCache<String, String>(testSize);

		for (int i = 1; i <= testSize; i++) {
			cache.set(itos(i), itos(i * i));
		}

		// query the elements from 5000 to 8999 (4000 elements) so that
		// they are marked as having been accessed recently
		for (int i = 5000; i <= 8999; i++) {
			assertEquals(cache.get(itos(i)), itos(i * i));
		}

		// insert 6000 new elements
		// => 1-4999 and 9000-testSize get removed
		for (int i = 10001; i <= 16000; i++) {
			cache.set(itos(i), itos(i * i));
		}

		// elements 1 to 4999 == null
		for (int i = 1; i < 5000; i++) {
			assertNull(cache.get(itos(i)));
		}
		// elements 9000 to testSize == null
		for (int i = 9000; i <= testSize; i++) {
			assertNull(cache.get(itos(i)));
		}
		// elements 5000 to 8999 are present
		for (int i = 5000; i < 9000; i++) {
			assertEquals(cache.get(itos(i)), itos(i * i));
		}
		// elements 10001 to 16000 are present
		for (int i = 10001; i <= 16000; i++) {
			assertEquals(cache.get(itos(i)), itos(i * i));
		}
	}

	@Test
	public void fullCacheWhereOldObjectGetRemoved3() {
		LruCache<String, String> cache = new LruCache<String, String>(100000);

		for (int i = 1; i <= 100000; i++) {
			cache.set(itos(i), itos(i * i));
		}

		// query every second element so that
		// they are marked as beeing accessed recently
		for (int i = 2; i <= 100000; i += 2) {
			assertEquals(cache.get(itos(i)), itos(i * i));
		}

		// insert 50000 new elements
		// => all odd numbers from 1-100000 get removed
		for (int i = 100001; i <= 150000; i++) {
			cache.set(itos(i), itos(i * i));
		}

		// all odd numbers from 1-100000 are null
		for (int i = 1; i < 100000; i += 2) {
			assertNull(cache.get(itos(i)));
		}
		// all even numbers are present
		for (int i = 2; i <= 100000; i += 2) {
			assertEquals(cache.get(itos(i)), itos(i * i));
		}
		// elements 100001 to 150000 are present
		for (int i = 100001; i <= 150000; i++) {
			assertEquals(cache.get(itos(i)), itos(i * i));
		}
	}
}
