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

public final class StagedCache<K, V> implements Cache<K, V> {

	/** The minimum capacity of a staged cache. */
	public static final int MIN_CACHE_CAPACITY = 128;

	/** The default capacity of an object cache. */
	public static final int DEFAULT_CACHE_CAPACITY = 1024;

	/** The default capacity of an object cache. */
	public static final double DEFAULT_OLD_GEN_RATIO = 0.25;

	public static final long DEFAULT_OLD_GEN_AFTER_HITS = 10;

	private final LruList<StagedCacheEntry<K, V>> lruYoungGen;

	private final LruList<StagedCacheEntry<K, V>> lruOldGen;

	private final ClusteredHashIndex<K, StagedCacheEntry<K, V>> hashIndex;

	public StagedCache() {
		this(DEFAULT_CACHE_CAPACITY);
	}

	public StagedCache(int capacity) {
		this(capacity, DEFAULT_OLD_GEN_RATIO);
	}

	public StagedCache(int capacity, double oldGenRatio) {
		this(capacity, oldGenRatio, DEFAULT_OLD_GEN_AFTER_HITS);
	}

	public StagedCache(int capacity, double oldGenRatio, long oldGenAfterHits) {

		capacity = Math.max(MIN_CACHE_CAPACITY, capacity);
		lruYoungGen = new LruList<StagedCacheEntry<K, V>>(youngCap);
		lruOldGen = new LruList<StagedCacheEntry<K, V>>(oldCap);
		hashIndex = new ClusteredHashIndex<K, StagedCacheEntry<K, V>>(capacity);
	}

	// TODO: remove code duplication
	@Override
	public void add(K key, V value) {
		Validation.notNull(key);
		StagedCacheEntry<K, V> entry = hashIndex.get(key);
		if (value == null) {
			if (entry != null) {
				remove(entry);
			}
			return;
		}
		if (entry == null) {
			entry = new StagedCacheEntry<K, V>(key, value);
			hashIndex.add(entry);
			StagedCacheEntry<K, V> removed = lruYoungGen.add(entry);
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
		StagedCacheEntry<K, V> entry = hashIndex.get(key);
		if (entry != null) {
			V value = entry.getValue();
			if (value != null) {
				// the soft-reference has not been collected by the gc
				if (entry.isInLruYoungGen()) {
					long hits = entry.recordHit();
					if (hits < oldGenAfterHits) {
						lruYoungGen.moveToHead(entry);
					}
					else {
						// TODO: make separete moveUp/moveDown methods
						lruYoungGen.remove(entry);
						entry.setInLruYoungGen(false);
						StagedCacheEntry<K, V> removed = lruOldGen.add(entry);
						if (removed != null) {
							if (removed.getValue() == null) {
								hashIndex.remove(removed);
							}
							else {
								removed.setInLruYoungGen(true);
								StagedCacheEntry<K, V> removed2 = lruYoungGen.add(removed);
								if (removed2 != null) {
									hashIndex.remove(removed2);
								}
							}
						}
					}
				}
				else {
					lruOldGen.moveToHead(entry);
				}
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

	private void remove(StagedCacheEntry<K, V> entry) {
		if (entry != null) {
			hashIndex.remove(entry);
			if (entry.isInLruYoungGen()) {
				lruYoungGen.remove(entry);
			}
			else {
				lruOldGen.remove(entry);
			}
		}
	}

	@Override
	public int capacity() {
		return capacity;
	}

	@Override
	public void clear() {
		lruYoungGen.clear();
		lruOldGen.clear();
		hashIndex.clear();
	}
}
