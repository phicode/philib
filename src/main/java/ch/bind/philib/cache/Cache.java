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

/**
 * The base interface for a cache implementation.
 * 
 * @author Philipp Meinen
 */
public interface Cache<K, V> {

	/** The default capacity of an object cache. */
	public static final int DEFAULT_CACHE_CAPACITY = 256;

	/**
	 * Add a key-value-pair to the cache.
	 * 
	 * @throws IllegalArgumentException if the key is {@code null}.
	 */
	void add(K key, V value);

	/**
	 * Query a value from the cache by its key.
	 * 
	 * @throws IllegalArgumentException if the key is {@code null}.
	 * @return null if no value for the given key was found. Otherwise the value for this key.
	 */
	V get(K key);

	/**
	 * Remove a key-value-pair from the cache.
	 * 
	 * @throws IllegalArgumentException if the key is {@code null}.
	 */
	void remove(K key);

	/**
	 * @return the capacity of this cache.
	 */
	int capacity();

	/** Remove all elements from the cache. */
	void clear();

}
