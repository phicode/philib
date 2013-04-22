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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ch.bind.philib.validation.Validation;

/**
 * @author Philipp Meinen
 */
public final class LineCache<K, V> implements Cache<K, V> {

	private static final int DEFAULT_ORDER = 4;

	// TODO: locks per bucket or just multiple locks
	private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

	private final Lock wlock = rwlock.writeLock();

	private final Lock rlock = rwlock.readLock();

	private final Entry<K, V>[] entries;

	private final int lines;

	private final int order;

	private final Cloner<V> valueCloner;

	private final AtomicLong accessCounter = new AtomicLong();

	public LineCache() {
		this(DEFAULT_CAPACITY, DEFAULT_ORDER, null);
	}

	public LineCache(int capacity, int order) {
		this(capacity, order, null);
	}

	public LineCache(Cloner<V> valueCloner) {
		this(DEFAULT_CAPACITY, DEFAULT_ORDER, valueCloner);
	}

	@SuppressWarnings("unchecked")
	public LineCache(int capacity, int order, Cloner<V> valueCloner) {
		Validation.isTrue(capacity > 0 && order > 0, "capacity and order must be greater than zero");
		Validation.isTrue(capacity % order == 0, "capacity must be a multiple of order");
		this.lines = capacity / order;
		this.order = order;
		this.entries = new Entry[capacity];
		this.valueCloner = valueCloner;
	}

	@Deprecated
	@Override
	public void add(final K key, final V value) {
		set(key, value);
	}

	@Override
	public void set(final K key, final V value) {
		Validation.notNull(key);
		Validation.notNull(value);
		int hash = key.hashCode();
		int line = Math.abs(hash) % lines;
		int startIdx = line * order;
		wlock.lock();
		try {
			int firstEmptyIndex = -1;
			Entry<K, V> leastAccessCounterEntry = null;
			int leastAccessIndex = -1;
			for (int o = 0; o < order; o++) {
				int idx = startIdx + o;
				Entry<K, V> e = entries[idx];
				if (e == null) {
					firstEmptyIndex = firstEmptyIndex == -1 ? idx : firstEmptyIndex;
					continue;
				}
				if (e.matches(key, hash)) {
					e.value = new SoftReference<V>(value);
					return;
				}
				if (leastAccessCounterEntry == null) {
					leastAccessCounterEntry = e;
					leastAccessIndex = idx;
				} else {
					if (e.lastAccess.get() < leastAccessCounterEntry.lastAccess.get()) {
						leastAccessCounterEntry = e;
						leastAccessIndex = idx;
					}
				}
			}
			if (firstEmptyIndex != -1) {
				entries[firstEmptyIndex] = new Entry<K, V>(key, hash, value);
			} else {
				entries[leastAccessIndex] = new Entry<K, V>(key, hash, value);
			}
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public V get(final K key) {
		Validation.notNull(key);
		int hash = key.hashCode();
		int line = Math.abs(hash) % lines;
		int startIdx = line * order;
		rlock.lock();
		try {
			for (int o = 0; o < order; o++) {
				int idx = startIdx + o;
				Entry<K, V> e = entries[idx];
				if (e != null && e.matches(key, hash)) {
					V value = e.value.get();
					if (value == null) {
						entries[idx] = null;
						return null;
					}
					long access = accessCounter.incrementAndGet();
					e.setLastAccessMaxVal(access);
					return valueCloner == null ? value : valueCloner.clone(value);
				}
			}
			return null;
		} finally {
			rlock.unlock();
		}
	}

	@Override
	public void remove(final K key) {
		Validation.notNull(key);
		int hash = key.hashCode();
		int line = Math.abs(hash) % lines;
		int startIdx = line * order;
		wlock.lock();
		try {
			for (int o = 0; o < order; o++) {
				int idx = startIdx + o;
				Entry<K, V> e = entries[idx];
				if (e != null && e.matches(key, hash)) {
					entries[idx] = null;
					return;
				}
			}
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public int capacity() {
		return lines * order;
	}

	@Override
	public void clear() {
		wlock.lock();
		try {
			Arrays.fill(entries, null);
		} finally {
			wlock.unlock();
		}
	}

	private static final class Entry<K, V> {

		final K key;

		final int hash;

		final AtomicLong lastAccess = new AtomicLong();

		SoftReference<V> value;

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
			return this.hash == h && this.key.equals(k);
		}
	}
}
