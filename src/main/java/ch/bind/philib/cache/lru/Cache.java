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
 * The base interface for a cache implementation.
 * 
 * @author Philipp Meinen
 */
public interface Cache<K, V> {

	/**
	 * Add a key-value-pair to the cache.
	 * 
	 * @throws IllegalArgumentException
	 *             if the key is {@code null}.
	 */
	public void add(K key, V value);

	/**
	 * Add a key-value-pair with a certain time-to-live (in milliseconds) to the cache.
	 * 
	 * @throws IllegalArgumentException
	 *             if the key is {@code null}.
	 */
	public void add(K key, V value, long ttlMs);

	/**
	 * Query a value from the cache by its key.
	 * 
	 * @throws IllegalArgumentException
	 *             if the key is {@code null}.
	 * @return null if no value for the given key was found. Otherwise the value
	 *         for this key.
	 */
	public V get(K key);

	/**
	 * Remove a key-value-pair from the cache.
	 * 
	 * @throws IllegalArgumentException
	 *             if the key is {@code null}.
	 */
	public void remove(K key);

	/**
	 * Tests if the cache contains a given key.
	 * 
	 * @param key
	 *            the key which needs to be be tested.
	 * @return <code>true</code> if a key-value-pair with the given
	 *         <code>key</code> is in the cache. <code>false</code> otherwise.
	 * @throws IllegalArgumentException
	 *             if the key is {@code null}.
	 */
	// public boolean contains(K key);

	/**
	 * Get the current number of objects in the cache.
	 * 
	 * This method may be slower then expected and should not normally be used!
	 * In order to correctly determine the size of a cache all timed-out pairs
	 * must be removed first. When you invoke this method you effectively invoke
	 * the {@link ICache#clearTimedOutPairs()} first.
	 * 
	 * @return the current number of objects in the cache.
	 */
	public int size();

	/**
	 * Tests whether the cache is empty or not.
	 * 
	 * @return <code>true</code> if the cache is empty. <code>false</code>
	 *         otherwise.
	 */
	public boolean isEmpty();

	/**
	 * Get the timeout in milliseconds.
	 * 
	 * @return the timeout in milliseconds.
	 */
	// public long getTimeout();

	/**
	 * Get the capacity of this cache.
	 * 
	 * @return the capacity of this cache.
	 */
	public int capacity();

	/** Remove all elements from the cache. */
	public void clear();

	/** Remove all pairs which reached their timeout. */
	// public void clearTimedOutPairs();

	/**
	 * Add a <code>RecycleListener</code> which must be invoked when a pair gets
	 * recycled.
	 */
	// public void addRecycleListener(RecycleListener<K, V> listener);

	/** Add a <code>RecycleListener</code>. */
	// public void removeRecycleListener(RecycleListener<K, V> listener);
}
