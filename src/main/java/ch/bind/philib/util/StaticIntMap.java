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

import ch.bind.philib.validation.Validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * A statically initialized and immutable map of {@code int -> T}.
 *
 * @author Philipp Meinen
 */
public final class StaticIntMap<T> {

	private final int[] keys;

	private final Object[] values;

	private StaticIntMap(int[] keys, Object[] values) {
		this.keys = keys;
		this.values = values;
	}

	/**
	 * @param elements
	 * @return A fully initialized {@code StaticIntMap}.
	 * @throws IllegalArgumentException If the {@code elements} parameter is {@code null}, <i>empty</i> or contains
	 *                                  duplicate keys.
	 * @throws NullPointerException     If any value in {@code elements} is a {@code null}-value.
	 */
	public static <T> StaticIntMap<T> create(Collection<? extends IntPair<T>> elements) {
		Validation.notNullOrEmpty(elements);
		final int l = elements.size();
		IntPair<?>[] elems = new IntPair<?>[l];
		elems = elements.toArray(elems);
		return init(elems);
	}

	/**
	 * @param elements
	 * @return A fully initialized {@code StaticIntMap}.
	 * @throws IllegalArgumentException If the {@code elements} parameter is {@code null}, <i>empty</i> or contains
	 *                                  duplicate keys.
	 * @throws NullPointerException     If any value in {@code elements} is a {@code null}-value.
	 */
	public static <T> StaticIntMap<T> create(IntPair<T>[] elements) {
		Validation.notNullOrEmpty(elements);
		// make a copy which we can sort so that we do not disturb the caller's
		// array
		elements = elements.clone();
		return init(elements);
	}

	private static <T> StaticIntMap<T> init(IntPair<?>[] elements) {
		int len = elements.length;
		Arrays.sort(elements, IntPair.KEY_COMPARATOR);
		int[] keys = new int[len];
		Object[] values = new Object[len];
		int prevKey = 0;
		for (int i = 0; i < len; i++) {
			IntPair<?> elem = elements[i];
			int key = elem.getKey();
			Object value = elem.getValue();
			if (i > 0 && prevKey == key) {
				throw new IllegalArgumentException("duplicate key: " + key);
			}
			prevKey = key;
			keys[i] = key;
			values[i] = value;
		}
		return new StaticIntMap<>(keys, values);
	}

	/**
	 * @param key
	 * @return The value associated with {@code key}, which may be {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public T get(int key) {
		final int idx = Arrays.binarySearch(keys, key);
		return (T) (idx < 0 ? null : values[idx]);
	}

	/**
	 * @param key
	 * @return The value associated with {@code key}, {@code defaultVal} otherwise.
	 */
	@SuppressWarnings("unchecked")
	public T getOrElse(int key, T defaultVal) {
		final int idx = Arrays.binarySearch(keys, key);
		return (T) (idx < 0 ? defaultVal : values[idx]);
	}

	/**
	 * @param key
	 * @return The value associated with {@code key}, which may be {@code null}.
	 * @throws NoSuchElementException If no value is associated with {@code key}.
	 */
	@SuppressWarnings("unchecked")
	public T getOrThrow(int key) throws NoSuchElementException {
		final int idx = Arrays.binarySearch(keys, key);
		if (idx < 0) {
			throw new NoSuchElementException("no value found for key: " + key);
		}
		return (T) values[idx];
	}

	public boolean containsKey(int key) {
		return Arrays.binarySearch(keys, key) >= 0;
	}

	public int size() {
		return keys.length;
	}
}
