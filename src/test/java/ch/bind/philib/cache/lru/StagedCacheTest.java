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

package ch.bind.philib.cache.lru;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

public class StagedCacheTest extends CacheTestBase {

	@Override
	<K, V> Cache<K, V> create() {
		return new StagedCache<K, V>();
	}

	@Override
	<K, V> Cache<K, V> create(int capacity) {
		return new StagedCache<K, V>(capacity);
	}

	@Override
	int getMinCapacity() {
		return StagedCache.MIN_CACHE_CAPACITY;
	}

	@Override
	int getDefaultCapacity() {
		return StagedCache.DEFAULT_CACHE_CAPACITY;
	}

	@Test
	public void stages() {
		final int cap = 100000;
		Cache<Integer, Integer> cache = new StagedCache<Integer, Integer>(cap, 0.5, 2);

		add(cache, 0, 50000);

		// 1 hit for 0-24999
		touch(cache, 0, 25000);

		// add 50000-74999 => 25000-49999 should vanish
		add(cache, 50000, 75000);

		// validate. afterwards we have the following hit rates:
		// 2 hits for 0-24999 => old-gen
		// removed: 25000-49999
		// 1 hit for 50000-74999 => still young gen
		touch(cache, 0, 25000);
		getNull(cache, 25000, 50000);
		touch(cache, 50000, 75000);

		// touch all keys
		// 3 hits for 0-24999 => still old gen
		// 2 hits for 50000-74999 => newly in old gen
		touch(cache, 0, 25000);
		getNull(cache, 25000, 50000);
		touch(cache, 50000, 75000);

		// fresh meat 75000-124999
		add(cache, 75000, 125000);

		// touch all
		// 4 hits for 0-24999 => still old gen
		// 3 hits for 50000-74999 => newly in old gen
		// 1 hit for 75000-124999
		touch(cache, 0, 25000);
		getNull(cache, 25000, 50000);
		touch(cache, 50000, 125000);

		// now touch 75000-124999 again so we end up with
		// 4 hits for 0-24999 => move to young gen
		// 3 hits for 50000-74999 => move to young gen
		// 2 hit for 75000-124999 => newly in old gen
		touch(cache, 75000, 125000);

		// add 50000 new elements which should evict 0-24999 and 50000-74999
		add(cache, 125000, 175000);
		getNull(cache, 0, 75000);
		touch(cache, 75000, 175000);
	}

	private static void add(Cache<Integer, Integer> cache, int from, int to) {
		for (int i = from; i < to; i++) {
			cache.add(i, Integer.valueOf(i * i));
		}
	}

	private static void touch(Cache<Integer, Integer> cache, int from, int to) {
		for (int i = from; i < to; i++) {
			assertEquals(cache.get(i), Integer.valueOf(i * i));
		}
	}

	private static void getNull(Cache<Integer, Integer> cache, int from, int to) {
		for (int i = from; i < to; i++) {
			assertNull(cache.get(i));
		}
	}
}
