package ch.bind.philib.cache.lru;

import static org.testng.Assert.*;

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
		for (int i = 0; i < cap; i++) {
			cache.add(i, i * i);
		}

		// 1 hit each
		for (int i = 0; i < cap; i++) {
			assertEquals(cache.get(i), Integer.valueOf(i * i));
		}

		// 2 hits for 0-49999
		for (int i = 0; i < 50000; i++) {
			assertEquals(cache.get(i), Integer.valueOf(i * i));
		}

		// add 100000-149999
		for (int i = 100000; i < 150000; i++) {
			cache.add(i, i * i);
		}

		for (int i = -10000; i < 200000; i++) {
			if ((i >= 0 && i < 50000) || (i >= 100000 && i < 150000)) {
				assertEquals(cache.get(i), Integer.valueOf(i * i));
			} else {
				assertNull(cache.get(i));
			}
		}
	}
}
