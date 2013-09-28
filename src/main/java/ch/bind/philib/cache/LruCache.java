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
import ch.bind.philib.util.ClusteredHashIndex;
import ch.bind.philib.util.ClusteredIndex;
import ch.bind.philib.util.LruList;
import ch.bind.philib.validation.Validation;

public final class LruCache<K, V> implements Cache<K, V> {

	private final LruList<LruCacheEntry<K, V>> lru;

	private final ClusteredIndex<K, LruCacheEntry<K, V>> index;

	private final Cloner<V> valueCloner;

	public LruCache() {
		this(DEFAULT_CAPACITY);
	}

	public LruCache(int capacity) {
		this(capacity, null);
	}

	public LruCache(Cloner<V> valueCloner) {
		this(DEFAULT_CAPACITY, valueCloner);
	}

	public LruCache(int capacity, Cloner<V> valueCloner) {
		Validation.isTrue(capacity > 0, "capacity must be greater than 0");
		this.lru = new LruList<LruCacheEntry<K, V>>(capacity);
		this.index = new ClusteredHashIndex<K, LruCacheEntry<K, V>>(capacity);
		this.valueCloner = valueCloner;
	}

	@Override
	public synchronized void set(final K key, final V value) {
		Validation.notNull(key);
		Validation.notNull(value);
		LruCacheEntry<K, V> entry = index.get(key);
		if (entry == null) {
			entry = new LruCacheEntry<K, V>(key, value);
			index.add(entry);
			LruCacheEntry<K, V> removed = lru.add(entry);
			if (removed != null) {
				index.remove(removed);
			}
		} else {
			entry.setValue(value);
		}
	}

	@Override
	public synchronized V get(final K key) {
		Validation.notNull(key);
		LruCacheEntry<K, V> entry = index.get(key);
		if (entry == null) {
			return null;
		}
		V value = entry.getValue();
		if (value == null) {
			// the soft-reference has been collected by the gc
			removeLruAndIndex(entry);
			return null;
		}
		lru.moveToHead(entry);
		return valueCloner == null ? value : valueCloner.clone(value);
	}

	@Override
	public synchronized void remove(final K key) {
		Validation.notNull(key);
		removeLruAndIndex(index.get(key));
	}

	@Override
	public synchronized int capacity() {
		return lru.capacity();
	}

	@Override
	public synchronized void clear() {
		lru.clear();
		index.clear();
	}

	private void removeLruAndIndex(final LruCacheEntry<K, V> entry) {
		if (entry != null) {
			index.remove(entry);
			lru.remove(entry);
		}
	}
}
