package ch.bind.philib.cache.lru.newimpl;

public class LruCache<K, V> implements Cache<K, V> {

	/** The minimum capacity of an object cache. */
	public static final int MIN_CACHE_CAPACITY = 64;

	/** The default capacity of an object cache. */
	public static final int DEFAULT_CACHE_CAPACITY = 1024;

	private final int capacity;

	private final int size;

	private LruCacheEntry<K, V> lruHead; // most recently accessed

	private LruCacheEntry<K, V> lruTail; // least recently accessed

	@Override
	public void add(Object key, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(Object key) {
		// TODO Auto-generated method stub

	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int capacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

}
