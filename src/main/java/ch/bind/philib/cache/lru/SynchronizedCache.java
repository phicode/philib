/*
 * Copyright (c) 2006 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.cache.lru;

/**
 * A synchronization class for a cache implementation.
 * 
 * @author Philipp Meinen
 */
// TODO: test java 1.5 locks for better performance
public final class SynchronizedCache<K, V> implements ICache<K, V> {

	private final ICache<K, V> cache;

	/**
	 * Create a new <code>SynchronizedCache</code>.
	 * 
	 * @param cache
	 *            the unsynchronized cache that one needs to access
	 *            concurrently.
	 * @throws IllegalArgumentException
	 *             If <code>cache</code> is <code>null</code>.
	 */
	public SynchronizedCache(ICache<K, V> cache) {
		if (cache == null)
			throw new IllegalArgumentException("cache must not be null.");
		this.cache = cache;
	}

	/**
	 * @see ICache#add(Object, Object)
	 */
	@Override
	public void add(K key, V value) {
		synchronized (cache) {
			cache.add(key, value);
		}
	}

	/**
	 * @see ICache#remove(Object)
	 */
	@Override
	public void remove(K key) {
		synchronized (cache) {
			cache.remove(key);
		}
	}

	/**
	 * @see ICache#contains(Object)
	 */
	@Override
	public boolean contains(K key) {
		synchronized (cache) {
			return cache.contains(key);
		}
	}

	/**
	 * @see ICache#clear()
	 */
	@Override
	public void clear() {
		synchronized (cache) {
			cache.clear();
		}
	}

	/**
	 * @see ICache#getCapacity()
	 */
	@Override
	public int getCapacity() {
		return cache.getCapacity();
	}

	/**
	 * @see ICache#size()
	 */
	@Override
	public int size() {
		synchronized (cache) {
			return cache.size();
		}
	}

	/**
	 * @see ICache#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		synchronized (cache) {
			return cache.isEmpty();
		}
	}

	/**
	 * @see ICache#get(Object)
	 */
	@Override
	public V get(K key) {
		synchronized (cache) {
			return cache.get(key);
		}
	}

	/**
	 * @see ICache#getTimeout()
	 */
	@Override
	public long getTimeout() {
		return cache.getTimeout();
	}

	/**
	 * @see ICache#clearTimedOutPairs()
	 */
	@Override
	public void clearTimedOutPairs() {
		synchronized (cache) {
			cache.clearTimedOutPairs();
		}
	}

	/**
	 * @see ICache#addRecycleListener(RecycleListener)
	 */
	@Override
	public void addRecycleListener(RecycleListener<K, V> listener) {
		synchronized (cache) {
			cache.addRecycleListener(listener);
		}
	}

	/**
	 * @see ICache#removeRecycleListener(RecycleListener)
	 */
	@Override
	public void removeRecycleListener(RecycleListener<K, V> listener) {
		synchronized (cache) {
			cache.removeRecycleListener(listener);
		}
	}
}
