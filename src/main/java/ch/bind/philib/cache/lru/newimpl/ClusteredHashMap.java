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

package ch.bind.philib.cache.lru.newimpl;

final class ClusteredHashMap<K, V, T extends ClusteredHashEntry<K, V>> {

	private final ClusteredHashEntry<K, V>[] table;

	ClusteredHashMap(int capacity) {
		table = new ClusteredHashEntry[capacity];
	}

	boolean add(final T entry) {
		assert (entry != null && entry.getNext() == null && entry.getKey() != null && entry.getValue() != null);

		final int hash = entry.cachedHash();
		final int position = hashPosition(hash);

		ClusteredHashEntry<K, V> now = table[position];
		if (now == null) {
			table[position] = entry;
			return true;
		}
		else {
			final K key = entry.getKey();
			ClusteredHashEntry<K, V> prev = null;
			while (now != null) {
				if (hash == now.cachedHash() && key.equals(now.getKey())) {
					// key is already in the table
					return false;
				}
				prev = now;
				now = now.getNext();
			}
			assert (prev != null);
			prev.setNext(now);
			return true;
		}
	}

	boolean remove(final T entry) {
		assert (entry != null);

		final K key = entry.getKey();
		final int hash = entry.cachedHash();
		final int position = hashPosition(hash);

		ClusteredHashEntry<K, V> prev = null;
		ClusteredHashEntry<K, V> now = table[position];
		while (now != null && now != entry) {
			prev = now;
			now = now.getNext();
		}
		if (now != null) {
			assert (hash == now.cachedHash() && key.equals(now.getKey()));
			if (prev == null) {
				// first entry in the table
				table[position] = now.getNext();
			}
			else {
				// there are entries before this one
				prev.setNext(now.getNext());
			}
			return true; // entry found and removed
		}
		return false; // entry not found
	}

	// returns null if a pair does not exist
	T get(final K key) {
		assert (key != null);

		final int hash = key.hashCode();
		final int position = hashPosition(hash);

		ClusteredHashEntry<K, V> entry = table[position];
		while (entry != null && hash != entry.cachedHash() && key.equals(entry.getKey()) == false) {
			entry = entry.getNext();
		}
		return (T) entry;
	}

	private int hashPosition(int hash) {
		int p = hash % table.length;
		return Math.abs(p);
	}
}
