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

package ch.bind.philib.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

import ch.bind.philib.validation.Validation;

/**
 * A statically initialized and immutable map of {@code long -> T}.
 * 
 * @author Philipp Meinen
 */
public final class StaticLongMap<T> {

	private final long[] keys;

	private final Object[] values;

	private StaticLongMap(long[] keys, Object[] values) {
		this.keys = keys;
		this.values = values;
	}

	/**
	 * 
	 * @param elements
	 * @return A fully initialized {@code StaticLongMap}.
	 * @throws IllegalArgumentException If the {@code elements} parameter is {@code null}, <i>empty</i> or contains
	 *             duplicate keys.
	 * @throws NullPointerException If any value in {@code elements} is a {@code null}-value.
	 */
	public static <T> StaticLongMap<T> create(Collection<? extends LongPair<T>> elements) {
		Validation.notNullOrEmpty(elements);
		final int l = elements.size();
		LongPair<?>[] elems = new LongPair<?>[l];
		elems = elements.toArray(elems);
		return init(elems);
	}

	/**
	 * @param elements
	 * @return A fully initialized {@code StaticLongMap}.
	 * @throws IllegalArgumentException If the {@code elements} parameter is {@code null}, <i>empty</i> or contains
	 *             duplicate keys.
	 * @throws NullPointerException If any value in {@code elements} is a {@code null}-value.
	 */
	public static <T> StaticLongMap<T> create(LongPair<T>[] elements) {
		Validation.notNullOrEmpty(elements);
		// make a copy which we can sort so that we do not disturb the caller's
		// array
		elements = elements.clone();
		return init(elements);
	}

	private static <T> StaticLongMap<T> init(LongPair<?>[] elements) {
		int len = elements.length;
		Arrays.sort(elements, LongPair.KEY_COMPARATOR);
		long[] keys = new long[len];
		Object[] values = new Object[len];
		long prevKey = 0;
		for (int i = 0; i < len; i++) {
			LongPair<?> elem = elements[i];
			long key = elem.getKey();
			Object value = elem.getValue();
			if (i > 0 && prevKey == key) {
				throw new IllegalArgumentException("duplicate key: " + key);
			}
			prevKey = key;
			keys[i] = key;
			values[i] = value;
		}
		return new StaticLongMap<T>(keys, values);
	}

	/**
	 * @param key
	 * @return The value associated with {@code key}, which may be {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public T get(long key) {
		final int idx = Arrays.binarySearch(keys, key);
		return (T) (idx < 0 ? null : values[idx]);
	}

	/**
	 * @param key
	 * @return The value associated with {@code key}, {@code defaultVal} otherwise.
	 */
	@SuppressWarnings("unchecked")
	public T getOrElse(long key, T defaultVal) {
		final int idx = Arrays.binarySearch(keys, key);
		return (T) (idx < 0 ? defaultVal : values[idx]);
	}

	/**
	 * @param key
	 * @return The value associated with {@code key}, which may be {@code null}.
	 * @throws NoSuchElementException If no value is associated with {@code key}.
	 */
	@SuppressWarnings("unchecked")
	public T getOrThrow(long key) throws NoSuchElementException {
		final int idx = Arrays.binarySearch(keys, key);
		if (idx < 0) {
			throw new NoSuchElementException("no value found for key: " + key);
		}
		return (T) values[idx];
	}

	public boolean containsKey(long key) {
		return Arrays.binarySearch(keys, key) >= 0;
	}

	public int size() {
		return keys.length;
	}
}
