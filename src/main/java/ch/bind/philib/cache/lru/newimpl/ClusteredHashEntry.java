package ch.bind.philib.cache.lru.newimpl;

public interface ClusteredHashEntry<K, V> {

	K getKey();

	int cachedHash();

	V getValue();

	ClusteredHashEntry<K, V> getNext();

	void setNext(ClusteredHashEntry<K, V> next);

}
