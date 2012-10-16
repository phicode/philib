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

package ch.bind.philib.cache.lru.newimpl;

import ch.bind.philib.validation.Validation;

//TODO: concurrent access
public final class SimpleCache<K, V> implements Cache<K, V> {

	/** The minimum capacity of a cache. */
	public static final int MIN_CACHE_CAPACITY = 8;

	/** The default capacity of an object cache. */
	public static final int DEFAULT_CACHE_CAPACITY = 1024;

	private final LruList<SimpleCacheEntry<K, V>> lru;

	private final ClusteredHashIndex<K, SimpleCacheEntry<K, V>> hashIndex;

	public SimpleCache() {
		this(DEFAULT_CACHE_CAPACITY);
	}

	public SimpleCache(int capacity) {
		capacity = Math.max(MIN_CACHE_CAPACITY, capacity);
		lru = new LruList<SimpleCacheEntry<K, V>>(capacity);
		hashIndex = new ClusteredHashIndex<K, SimpleCacheEntry<K, V>>(capacity);
	}

	@Override
	public void add(K key, V value) {
		Validation.notNull(key);
		SimpleCacheEntry<K, V> entry = hashIndex.get(key);
		if (value == null) {
			if (entry != null) {
				remove(entry);
			}
			return;
		}
		if (entry == null) {
			entry = new SimpleCacheEntry<K, V>(key, value);
			hashIndex.add(entry);
			SimpleCacheEntry<K, V> removed = lru.add(entry);
			if (removed != null) {
				hashIndex.remove(removed);
			}
		}
		else {
			entry.setValue(value);
		}
	}

	@Override
	public V get(K key) {
		Validation.notNull(key);
		SimpleCacheEntry<K, V> entry = hashIndex.get(key);
		if (entry != null) {
			V value = entry.getValue();
			if (value != null) {
				// the soft-reference has not been collected by the gc
				lru.moveToHead(entry);
				return value;
			}
			remove(key);
		}
		return null;
	}

	@Override
	public void remove(K key) {
		Validation.notNull(key);
		remove(hashIndex.get(key));
	}

	private void remove(SimpleCacheEntry<K, V> entry) {
		if (entry != null) {
			hashIndex.remove(entry);
			lru.remove(entry);
		}
	}

	@Override
	public int capacity() {
		return lru.capacity();
	}

	@Override
	public void clear() {
		lru.clear();
		hashIndex.clear();
	}
}
