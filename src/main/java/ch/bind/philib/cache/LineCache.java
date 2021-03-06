/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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
import ch.bind.philib.lang.ClonerNoop;
import ch.bind.philib.lang.MurmurHash;
import ch.bind.philib.validation.Validation;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author Philipp Meinen
 */
public final class LineCache<K, V> implements Cache<K, V> {

	static final int DEFAULT_ORDER = 8;

	private final AtomicReferenceArray<Entry<K, V>> entries;
	private final AtomicLong[] lineClocks;
	private final Cloner<V> valueCloner;

	private final int lineMask;
	private final int order;

	public LineCache() {
		this(DEFAULT_CAPACITY, DEFAULT_ORDER, null);
	}

	public LineCache(int capacity, int order) {
		this(capacity, order, null);
	}

	public LineCache(Cloner<V> valueCloner) {
		this(DEFAULT_CAPACITY, DEFAULT_ORDER, valueCloner);
	}

	public LineCache(int capacity, int order, Cloner<V> valueCloner) {
		Validation.isTrue(capacity > 0 && order > 0, "capacity and order must be greater than zero");
		Validation.isTrue(Integer.bitCount(order) == 1, "order must be a power of two");
		Validation.isTrue(capacity % order == 0, "capacity must be a multiple of order");

		int lines = capacity / order;
		this.entries = new AtomicReferenceArray<>(capacity);
		this.lineClocks = new AtomicLong[lines];
		for (int i = 0; i < lines; i++) {
			lineClocks[i] = new AtomicLong();
		}
		this.valueCloner = ClonerNoop.getIfNull(valueCloner);
		this.order = order;
		this.lineMask = lines - 1;
	}

	@Override
	public void set(final K key, final V value) {
		Validation.notNull(key);
		Validation.notNull(value);

		final int hash = hash(key);
		final int line = Math.abs(hash) & lineMask;
		final int startIdx = line * order;
		final int endIdx = startIdx + order;
		final long clock = lineClocks[line].getAndIncrement();
		final Entry<K, V> newEntry = new Entry<>(clock, key, hash, value);

		while (true) {
			int emptyIdx = -1;
			int lowestClockIdx = -1;
			Entry<K, V> lowestClock = null;

			for (int i = startIdx; i < endIdx; i++) {
				final Entry<K, V> e = entries.get(i);
				if (e == null) {
					// possible insertion location
					emptyIdx = i;
					continue;
				}
				if (e.matches(key, hash)) {
					// override existing entries if we are not dealing with a concurrent update
					if (e.clock < clock) {
						entries.compareAndSet(i, e, newEntry);
					}
					return;
				}
				if (lowestClock == null || e.clock < lowestClock.clock) {
					lowestClock = e;
					lowestClockIdx = i;
				}
			}
			if (emptyIdx != -1 && entries.compareAndSet(emptyIdx, null, newEntry)) {
				return;
			}
			if (lowestClockIdx != -1 && entries.compareAndSet(lowestClockIdx, lowestClock, newEntry)) {
				return;
			}
		}
	}

	private static int hash(Object o) {
		return MurmurHash.murmur3_finalize_mix32(o.hashCode());
	}

	@Override
	public V get(final K key) {
		Validation.notNull(key);

		final int hash = hash(key);
		final int line = Math.abs(hash) & lineMask;
		final int startIdx = line * order;
		final int endIdx = startIdx + order;

		Entry<K, V> found = null;
		for (int i = startIdx; i < endIdx; i++) {
			final Entry<K, V> e = entries.get(i);
			if (e == null || !e.matches(key, hash)) {
				continue;
			}
			if (found == null) {
				found = e;
			} else if (e.clock > found.clock) {
				// newer entry found
				entries.compareAndSet(i, found, null);
				found = e;
			}
		}
		return found == null ? null : valueCloner.clone(found.value);
	}

	@Override
	public void remove(final K key) {
		Validation.notNull(key);

		final int hash = hash(key);
		final int line = Math.abs(hash) & lineMask;
		final int startIdx = line * order;

		for (int o = 0; o < order; o++) {
			final int idx = startIdx + o;
			final Entry<K, V> e = entries.get(idx);
			if (e != null && e.matches(key, hash)) {
				entries.compareAndSet(idx, e, null);
				return;
			}
		}
	}

	@Override
	public int capacity() {
		return entries.length();
	}

	@Override
	public void clear() {
		final int cap = entries.length();
		for (int i = 0; i < cap; i++) {
			entries.lazySet(i, null);
		}
		// write fence
		entries.set(0, null);
	}

	private static final class Entry<K, V> {
		final long clock;
		final K key;
		final V value;
		final int hash;

		public Entry(long clock, K key, int hash, V value) {
			this.clock = clock;
			this.key = key;
			this.value = value;
			this.hash = hash;
		}

		boolean matches(final K k, final int h) {
			return this.hash == h && (this.key == k || this.key.equals(k));
		}
	}
}
