package ch.bind.philib.cache.lru.newimpl;

final class LruCacheEntry<K, V> {

	LruCacheEntry(K key, V value) {
		this.key = key;
		this.value = value;
		hash = key.hashCode();
	}

	private final K key;

	private final int hash;

	private final V value;

	private LruCacheEntry<K, V> nextInHash;

	private LruCacheEntry<K, V> lruYounger;

	private LruCacheEntry<K, V> lruOlder;

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}
