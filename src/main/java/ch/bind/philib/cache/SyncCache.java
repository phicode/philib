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
	@Deprecated
	public void add(final K key, final V value) {
		set(key, value);
	}

	@Override
	public void set(K key, V value) {
		lock.lock();
		try {
			cache.set(key, value);
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
