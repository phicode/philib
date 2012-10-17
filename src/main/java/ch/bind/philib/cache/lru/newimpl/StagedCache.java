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

	private static final double MIN_OLD_GEN_RATIO = 0.1;

	private static final double MAX_OLD_GEN_RATIO = 0.9;

	private final LruList<StagedCacheEntry<K, V>> lruYoungGen;

	private final LruList<StagedCacheEntry<K, V>> lruOldGen;

	private final ClusteredHashIndex<K, StagedCacheEntry<K, V>> index;

	private final long oldGenAfterHits;

	private final int capacity;

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
		this.capacity = Math.max(MIN_CACHE_CAPACITY, capacity);
		this.oldGenAfterHits = oldGenAfterHits;
		oldGenRatio = clip(oldGenRatio, MIN_OLD_GEN_RATIO, MAX_OLD_GEN_RATIO);
		int oldCap = (int) (this.capacity * oldGenRatio);
		int youngCap = this.capacity - oldCap;
		// TODO: >=10 % && <= 90%
		lruYoungGen = new LruList<StagedCacheEntry<K, V>>(youngCap);
		lruOldGen = new LruList<StagedCacheEntry<K, V>>(oldCap);
		index = new ClusteredHashIndex<K, StagedCacheEntry<K, V>>(capacity);
	}

	// TODO: move to a utility class
	private static double clip(double value, double min, double max) {
		return value < min ? min : (value > max ? max : value);
	}

	// TODO: remove code duplication
	@Override
	public void add(final K key, final V value) {
		Validation.notNull(key);
		StagedCacheEntry<K, V> entry = index.get(key);
		if (value == null) {
			if (entry != null) {
				removeLruAndIndex(entry);
			}
			return;
		}
		if (entry == null) {
			entry = new StagedCacheEntry<K, V>(key, value);
			index.add(entry);
			addYoungGen(entry, false);
		}
		else {
			entry.setValue(value);
		}
	}

	@Override
	public V get(final K key) {
		Validation.notNull(key);
		final StagedCacheEntry<K, V> entry = index.get(key);
		if (entry == null) {
			return null;
		}
		final V value = entry.getValue();
		if (value == null) {
			// the soft-reference has been collected by the gc
			removeLruAndIndex(entry);
			return null;
		}
		if (entry.isInLruYoungGen()) {
			int hits = entry.recordHit();
			if (hits >= oldGenAfterHits) {
				entry.resetHits();
				lruYoungGen.remove(entry);
				addOldGen(entry);
			}
			else {
				lruYoungGen.moveToHead(entry);
			}
		}
		else {
			lruOldGen.moveToHead(entry);
		}
		return value;
	}

	@Override
	public void remove(final K key) {
		Validation.notNull(key);
		removeLruAndIndex(index.get(key));
	}

	@Override
	public int capacity() {
		return capacity;
	}

	@Override
	public void clear() {
		lruYoungGen.clear();
		lruOldGen.clear();
		index.clear();
	}

	private void removeLruAndIndex(final StagedCacheEntry<K, V> entry) {
		if (entry != null) {
			index.remove(entry);
			if (entry.isInLruYoungGen()) {
				lruYoungGen.remove(entry);
			}
			else {
				lruOldGen.remove(entry);
			}
		}
	}

	private void addYoungGen(final StagedCacheEntry<K, V> entry, final boolean checkValue) {
		if (entry != null) {
			if (checkValue && entry.getValue() == null) {
				index.remove(entry);
			}
			else {
				entry.setInYoungGen();
				StagedCacheEntry<K, V> removed = lruYoungGen.add(entry);
				if (removed != null) {
					index.remove(removed);
				}
			}
		}
	}

	private void addOldGen(final StagedCacheEntry<K, V> entry) {
		if (entry != null) {
			if (entry.getValue() == null) {
				index.remove(entry);
			}
			else {
				entry.setInOldGen();
				StagedCacheEntry<K, V> removed = lruOldGen.add(entry);
				if (removed != null) {
					addYoungGen(removed, true);
				}
			}
		}
	}
}
