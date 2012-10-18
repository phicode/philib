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

public final class ClusteredHashIndex<K, T extends ClusteredIndexEntry<K>> {

	private final ClusteredIndexEntry<K>[] table;

	@SuppressWarnings("unchecked")
	public ClusteredHashIndex(int capacity) {
		table = new ClusteredIndexEntry[capacity];
	}

	public 	boolean add(final T entry) {
		assert (entry != null && entry.getNextIndexEntry() == null && entry.getKey() != null);

		final K key = entry.getKey();
		final int hash = key.hashCode();
		final int position = hashPosition(hash);

		ClusteredIndexEntry<K> scanNow = table[position];
		if (scanNow == null) {
			table[position] = entry;
			return true;
		}
		else {
			ClusteredIndexEntry<K> scanPrev = null;
			while (scanNow != null) {
				K nowKey = scanNow.getKey();
				if (hash == nowKey.hashCode() && key.equals(nowKey)) {
					// key is already in the table
					return false;
				}
				scanPrev = scanNow;
				scanNow = scanNow.getNextIndexEntry();
			}
			assert (scanPrev != null);
			scanPrev.setNextIndexEntry(entry);
			return true;
		}
	}

	public boolean remove(final T entry) {
		assert (entry != null);

		final K key = entry.getKey();
		final int hash = key.hashCode();
		final int position = hashPosition(hash);

		ClusteredIndexEntry<K> scanPrev = null;
		ClusteredIndexEntry<K> scanNow = table[position];
		while (scanNow != null && scanNow != entry) {
			scanPrev = scanNow;
			scanNow = scanNow.getNextIndexEntry();
		}
		if (scanNow != null) {
			assert (hash == scanNow.getKey().hashCode() && key.equals(scanNow.getKey()));
			if (scanPrev == null) {
				// first entry in the table
				table[position] = scanNow.getNextIndexEntry();
			}
			else {
				// there are entries before this one
				scanPrev.setNextIndexEntry(scanNow.getNextIndexEntry());
			}
			return true; // entry found and removed
		}
		return false; // entry not found
	}

	// returns null if a pair does not exist
	@SuppressWarnings("unchecked")
	public T get(final K key) {
		assert (key != null);

		final int hash = key.hashCode();
		final int position = hashPosition(hash);

		ClusteredIndexEntry<K> entry = table[position];
		while (entry != null) {
			K entryKey = entry.getKey();
			if (hash == entryKey.hashCode() && key.equals(entryKey)) {
				return (T) entry;
			}
			entry = entry.getNextIndexEntry();
		}
		return null;
	}

	private int hashPosition(int hash) {
		int p = hash % table.length;
		return Math.abs(p);
	}

	public void clear() {
		Arrays.fill(table, null);
	}
}
