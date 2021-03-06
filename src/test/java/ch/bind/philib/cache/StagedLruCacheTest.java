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

import ch.bind.philib.lang.Cloner;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test
public class StagedLruCacheTest extends CacheTestBase {

	@Override
	<K, V> Cache<K, V> create() {
		return new StagedLruCache<>();
	}

	@Override
	<K, V> Cache<K, V> create(int capacity) {
		return new StagedLruCache<>(capacity);
	}

	@Override
	<K, V> Cache<K, V> create(Cloner<V> valueCloner) {
		return new StagedLruCache<>(valueCloner);
	}

	@Override
	int getMinCapacity() {
		return (int) (1 / StagedLruCache.DEFAULT_OLD_GEN_RATIO);
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
	public void stages() {
		final int cap = 100000;
		Cache<Integer, Integer> cache = new StagedLruCache<>(cap, null, 0.5, 2);

		set(cache, 0, 50000);

		// 1 hit for 0-24999
		touch(cache, 0, 25000);

		// set 50000-74999 => 25000-49999 should vanish
		set(cache, 50000, 75000);

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
		set(cache, 75000, 125000);

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

		// set 50000 new elements which should evict 0-24999 and 50000-74999
		set(cache, 125000, 175000);
		getNull(cache, 0, 75000);
		touch(cache, 75000, 175000);
	}

	@Test
	public void removeOldEntry() {
		// 1 entry old generation & 1 young generation
		Cache<Integer, Integer> cache = new StagedLruCache<>(2, null, 0.5, 2);
		cache.set(1, 1);

		// 'elevate' 1 to old-gen
		cache.get(1);
		cache.get(1);

		for (int i = 2; i < 100000; i++) {
			assertNull(cache.get(i));
			cache.set(i, i * i);
			// 1 hit
			assertEquals(cache.get(i), Integer.valueOf(i * i));
		}

		assertEquals(cache.get(1), Integer.valueOf(1));
		cache.remove(1);
		assertNull(cache.get(1));
	}

	private static void set(Cache<Integer, Integer> cache, int from, int to) {
		for (int i = from; i < to; i++) {
			cache.set(i, i * i);
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
