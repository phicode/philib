package ch.bind.philib.cache.lru;

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
}
