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
import ch.bind.philib.math.Calc;
import ch.bind.philib.util.ClusteredHashIndex;
import ch.bind.philib.util.ClusteredIndex;
import ch.bind.philib.util.LruList;
import ch.bind.philib.validation.Validation;

public final class StagedLruCache<K, V> implements Cache<K, V> {

	/** The default capacity of an object cache relative to its capacity. */
	public static final double DEFAULT_OLD_GEN_RATIO = 0.25;

	/** The number of hits after which an entry is put into the old-generation lru */
	public static final int DEFAULT_OLD_GEN_AFTER_HITS = 10;

	private static final double MIN_OLD_GEN_RATIO = 0.1;

	private static final double MAX_OLD_GEN_RATIO = 0.9;

	private final LruList<StagedLruCacheEntry<K, V>> lruYoungGen;

	private final LruList<StagedLruCacheEntry<K, V>> lruOldGen;

	private final ClusteredIndex<K, StagedLruCacheEntry<K, V>> index;

	private final int oldGenAfterHits;

	private final int capacity;

	private final Cloner<V> valueCloner;

	public StagedLruCache() {
		this(DEFAULT_CAPACITY);
	}

	public StagedLruCache(int capacity) {
		this(capacity, null, DEFAULT_OLD_GEN_RATIO, DEFAULT_OLD_GEN_AFTER_HITS);
	}

	public StagedLruCache(Cloner<V> valueCloner) {
		this(DEFAULT_CAPACITY, valueCloner, DEFAULT_OLD_GEN_RATIO, DEFAULT_OLD_GEN_AFTER_HITS);
	}

	public StagedLruCache(int capacity, Cloner<V> valueCloner, double oldGenRatio, int oldGenAfterHits) {
		Validation.isTrue(capacity > 0, "capacity must be greater than 0");

		this.capacity = capacity;
		this.oldGenAfterHits = Math.max(1, oldGenAfterHits);
		oldGenRatio = Calc.clip(oldGenRatio, MIN_OLD_GEN_RATIO, MAX_OLD_GEN_RATIO);
		int oldCap = (int) (this.capacity * oldGenRatio);
		int youngCap = this.capacity - oldCap;
		this.lruYoungGen = new LruList<StagedLruCacheEntry<K, V>>(youngCap);
		this.lruOldGen = new LruList<StagedLruCacheEntry<K, V>>(oldCap);
		this.index = new ClusteredHashIndex<K, StagedLruCacheEntry<K, V>>(capacity);
		this.valueCloner = valueCloner;
	}

	// TODO: remove code duplication with LruCache
	@Override
	public synchronized void set(final K key, final V value) {
		Validation.notNull(key);
		Validation.notNull(value);
		StagedLruCacheEntry<K, V> entry = index.get(key);
		if (entry == null) {
			entry = new StagedLruCacheEntry<K, V>(key, value);
			index.add(entry);
			addYoungGen(entry, false);
		}
		else {
			entry.setValue(value);
		}
	}

	@Override
	public synchronized V get(final K key) {
		Validation.notNull(key);
		final StagedLruCacheEntry<K, V> entry = index.get(key);
		if (entry == null) {
			return null;
		}
		final V value = entry.getValue();
		if (value == null) {
			// the soft-reference has been collected by the gc
			removeLruAndIndex(entry);
			return null;
		}
		if (entry.isInYoungGen()) {
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
		return valueCloner == null ? value : valueCloner.clone(value);
	}

	@Override
	public synchronized void remove(final K key) {
		Validation.notNull(key);
		removeLruAndIndex(index.get(key));
	}

	@Override
	public synchronized int capacity() {
		return capacity;
	}

	@Override
	public synchronized void clear() {
		lruYoungGen.clear();
		lruOldGen.clear();
		index.clear();
	}

	private void removeLruAndIndex(final StagedLruCacheEntry<K, V> entry) {
		if (entry != null) {
			index.remove(entry);
			if (entry.isInYoungGen()) {
				lruYoungGen.remove(entry);
			}
			else {
				lruOldGen.remove(entry);
			}
		}
	}

	private void addYoungGen(final StagedLruCacheEntry<K, V> entry, final boolean checkValue) {
		if (checkValue && entry.getValue() == null) {
			index.remove(entry);
		}
		else {
			entry.setInYoungGen();
			StagedLruCacheEntry<K, V> removed = lruYoungGen.add(entry);
			if (removed != null) {
				// TODO: move to old gen if it has room
				index.remove(removed);
			}
		}
	}

	private void addOldGen(final StagedLruCacheEntry<K, V> entry) {
		if (entry.getValue() == null) {
			index.remove(entry);
		}
		else {
			entry.setInOldGen();
			StagedLruCacheEntry<K, V> removed = lruOldGen.add(entry);
			if (removed != null) {
				addYoungGen(removed, true);
			}
		}
	}
}
