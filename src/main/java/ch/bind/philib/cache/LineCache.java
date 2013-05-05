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

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import ch.bind.philib.lang.Cloner;
import ch.bind.philib.lang.MurmurHash;
import ch.bind.philib.validation.Validation;

/**
 * @author Philipp Meinen
 */
public final class LineCache<K, V> implements Cache<K, V> {

	static final int DEFAULT_ORDER = 8;

	private final AtomicReferenceArray<Entry<K, V>> entries;

	private final int lineMask;

	private final int order;

	private final Cloner<V> valueCloner;

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
		this.order = order;
		this.entries = new AtomicReferenceArray<Entry<K, V>>(capacity);
		this.valueCloner = valueCloner;
		this.lineMask = lines - 1;
	}

	@Override
	public void set(final K key, final V value) {
		Validation.notNull(key);
		Validation.notNull(value);

		final int hash = hash(key);
		final int line = Math.abs(hash) & lineMask;
		final int startIdx = line * order;

		final Entry<K, V> newe = new Entry<K, V>(key, hash, value);

		while (true) {
			int insertIdx = -1;
			Entry<K, V> overwrite = null;

			for (int o = 0; o < order; o++) {
				final int idx = startIdx + o;
				final Entry<K, V> e = entries.get(idx);
				if (e == null) {
					if (overwrite != null || insertIdx == -1) {
						insertIdx = idx;
						overwrite = null;
					}
					continue;
				}
				if (e.matches(key, hash)) {
					if (entries.compareAndSet(idx, e, newe)) {
						return;
					}
					break; // repeat while
				}
				if (insertIdx == -1) {
					insertIdx = idx;
					overwrite = e;
				}
				else if (overwrite != null && e.lastAccess.get() < overwrite.lastAccess.get()) {
					// System.out.println("facour throwing away " + e.key + " instead of " + overwrite.key +
					// " access diff: " //
					// + (overwrite.lastAccess.get() - e.lastAccess.get()));
					insertIdx = idx;
					overwrite = e;
				}
			}
			if (entries.compareAndSet(insertIdx, overwrite, newe)) {
				return;
			}
		}
	}

	private static final int hash(Object o) {
		return MurmurHash.murmur3_finalize_mix32(o.hashCode());
	}

	@Override
	public V get(final K key) {
		Validation.notNull(key);

		final int hash = hash(key);
		final int line = Math.abs(hash) & lineMask;
		final int startIdx = line * order;

		for (int o = 0; o < order; o++) {
			final int idx = startIdx + o;
			final Entry<K, V> e = entries.get(idx);
			if (e != null && e.matches(key, hash)) {
				final V value = e.value.get();
				if (value == null) {
					// soft-reference was collected
					entries.compareAndSet(idx, e, null);
					return null;
				}
				e.setLastAccessMaxVal(System.nanoTime());
				return valueCloner == null ? value : valueCloner.clone(value);
			}
		}
		return null;
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

		final K key;

		final int hash;

		final AtomicLong lastAccess = new AtomicLong();

		final SoftReference<V> value;

		public Entry(K key, int hash, V value) {
			this.key = key;
			this.hash = hash;
			this.value = new SoftReference<V>(value);
		}

		public void setLastAccessMaxVal(long access) {
			long v = lastAccess.get();
			while (access > v) {
				if (lastAccess.compareAndSet(v, access)) {
					return;
				}
				v = lastAccess.get();
			}
		}

		boolean matches(final K k, final int h) {
			return this.hash == h && (this.key == k || this.key.equals(k));
		}
	}
}
