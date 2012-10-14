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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//TODO: documentation
//TODO: benchmarks
public class MutexLockedCache<K, V> implements ICache<K, V> {

    private ICache<K, V> cache;
    private final Lock mutex;

    /**
     * Create a new <code>SynchronizedCache</code>.
     * 
     * @param cache
     *            the unsynchronized cache that one needs to access
     *            concurrently.
     * @throws IllegalArgumentException
     *             If <code>cache</code> is <code>null</code>.
     */
    public MutexLockedCache(ICache<K, V> cache) {
        if (cache == null)
            throw new IllegalArgumentException("cache must not be null.");
        mutex = new ReentrantLock();
        this.cache = cache;
    }

    /**
     * @see ICache#add(Object, Object)
     */
    @Override
	public void add(K key, V value) {
        mutex.lock();
        try {
            cache.add(key, value);
        } finally {
            mutex.unlock();
        }
    }

    /**
     * @see ICache#remove(Object)
     */
    @Override
	public void remove(K key) {
        mutex.lock();
        try {
            cache.remove(key);
        } finally {
            mutex.unlock();
        }
    }

    /**
     * @see ICache#contains(Object)
     */
    @Override
	public boolean contains(K key) {
        mutex.lock();
        try {
            return cache.contains(key);
        } finally {
            mutex.unlock();
        }
    }

    /**
     * @see ICache#clear()
     */
    @Override
	public void clear() {
        mutex.lock();
        try {
            cache.clear();
        } finally {
            mutex.unlock();
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
        mutex.lock();
        try {
            return cache.size();
        } finally {
            mutex.unlock();
        }
    }

    /**
     * @see ICache#isEmpty()
     */
    @Override
	public boolean isEmpty() {
        mutex.lock();
        try {
            return cache.isEmpty();
        } finally {
            mutex.unlock();
        }
    }

    /**
     * @see ICache#get(Object)
     */
    @Override
	public V get(K key) {
        mutex.lock();
        try {
            return cache.get(key);
        } finally {
            mutex.unlock();
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
        mutex.lock();
        try {
            cache.clearTimedOutPairs();
        } finally {
            mutex.unlock();
        }
    }

    /**
     * @see ICache#addRecycleListener(RecycleListener)
     */
    @Override
	public void addRecycleListener(RecycleListener<K, V> listener) {
        mutex.lock();
        try {
            cache.addRecycleListener(listener);
        } finally {
            mutex.unlock();
        }
    }

    /**
     * @see ICache#removeRecycleListener(RecycleListener)
     */
    @Override
	public void removeRecycleListener(RecycleListener<K, V> listener) {
        mutex.lock();
        try {
            cache.removeRecycleListener(listener);
        } finally {
            mutex.unlock();
        }
    }
}
