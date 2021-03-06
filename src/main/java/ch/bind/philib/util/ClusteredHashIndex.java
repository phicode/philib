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

import ch.bind.philib.lang.MurmurHash;
import ch.bind.philib.util.ClusteredIndex.Entry;
import ch.bind.philib.validation.Validation;

import java.util.Arrays;

// TODO: round table size up (2^x) and use bitmasks
// TODO: concurrent version
public final class ClusteredHashIndex<K, T extends Entry<K>> implements ClusteredIndex<K, T> {

	private final Entry<K>[] table;

	@SuppressWarnings("unchecked")
	public ClusteredHashIndex(int capacity) {
		table = new Entry[capacity];
	}

	@Override
	public boolean add(final T entry) {
		Validation.isTrue(entry != null && entry.getNextIndexEntry() == null && entry.getKey() != null, //
				"newly added entries must be non-null and cleared");

		final K key = entry.getKey();
		final int hash = key.hashCode();
		final int position = hashPosition(hash);

		Entry<K> scanNow = table[position];
		if (scanNow == null) {
			table[position] = entry;
			return true;
		}
		Entry<K> scanPrev = null;
		do {
			K nowKey = scanNow.getKey();
			if (hash == nowKey.hashCode() && key.equals(nowKey)) {
				// key is already in the table
				return false;
			}
			scanPrev = scanNow;
			scanNow = scanNow.getNextIndexEntry();
		} while (scanNow != null);
		scanPrev.setNextIndexEntry(entry);
		return true;
	}

	@Override
	public boolean remove(final T entry) {
		Validation.notNull(entry);

		final K key = entry.getKey();
		final int hash = key.hashCode();
		final int position = hashPosition(hash);

		Entry<K> scanPrev = null;
		Entry<K> scanNow = table[position];
		while (scanNow != null && scanNow != entry) {
			scanPrev = scanNow;
			scanNow = scanNow.getNextIndexEntry();
		}
		if (scanNow == entry) {
			if (scanPrev == null) {
				// first entry in the table
				table[position] = entry.getNextIndexEntry();
			} else {
				// there are entries before this one
				scanPrev.setNextIndexEntry(entry.getNextIndexEntry());
			}
			entry.setNextIndexEntry(null);
			return true; // entry found and removed
		}
		return false; // entry not found
	}

	// returns null if a pair does not exist
	@Override
	@SuppressWarnings("unchecked")
	public T get(final K key) {
		Validation.notNull(key);

		final int hash = key.hashCode();
		final int position = hashPosition(hash);

		Entry<K> entry = table[position];
		while (entry != null) {
			final K entryKey = entry.getKey();
			if (key == entryKey || (hash == entryKey.hashCode() && key.equals(entryKey))) {
				return (T) entry;
			}
			entry = entry.getNextIndexEntry();
		}
		return null;
	}

	private int hashPosition(int hash) {
		hash = MurmurHash.murmur3_finalize_mix32(hash);
		int p = hash % table.length;
		return Math.abs(p);
	}

	@Override
	public void clear() {
		for (Entry<K> e : table) {
			while (e != null) {
				Entry<K> next = e.getNextIndexEntry();
				e.setNextIndexEntry(null);
				e = next;
			}
		}
		Arrays.fill(table, null);
	}
}
