package ch.bind.philib.cache.lru;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.bind.philib.validation.Validation;

public final class SyncCache<K, V> implements Cache<K, V> {

	private final Lock lock = new ReentrantLock();

	private final Cache<K, V> cache;

	public SyncCache(Cache<K, V> cache) {
		Validation.notNull(cache);
		this.cache = cache;
	}

	public static final <K, V> Cache<K, V> wrap(Cache<K, V> cache) {
		return new SyncCache<K, V>(cache);
	}

	@Override
	public void add(K key, V value) {
		lock.lock();
		try {
			cache.add(key, value);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V get(K key) {
		lock.lock();
		try {
			return cache.get(key);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void remove(K key) {
		lock.lock();
		try {
			cache.remove(key);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int capacity() {
		return cache.capacity();
	}

	@Override
	public void clear() {
		lock.lock();
		try {
			cache.clear();
		} finally {
			lock.unlock();
		}
	}
}
