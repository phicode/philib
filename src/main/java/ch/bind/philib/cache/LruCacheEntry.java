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

import java.lang.ref.SoftReference;

import ch.bind.philib.util.ClusteredIndex.Entry;
import ch.bind.philib.util.LruNode;

class LruCacheEntry<K, V> implements Entry<K>, LruNode {

	private final K key;

	private SoftReference<V> value;

	private Entry<K> nextIndexEntry;

	private LruNode lruNext;

	private LruNode lruPrev;

	LruCacheEntry(K key, V value) {
		this.key = key;
		setValue(value);
	}

	@Override
	public K getKey() {
		return key;
	}

	V getValue() {
		return value.get();
	}

	void setValue(V value) {
		this.value = new SoftReference<V>(value);
	}

	@Override
	public Entry<K> getNextIndexEntry() {
		return nextIndexEntry;
	}

	@Override
	public void setNextIndexEntry(Entry<K> nextIndexEntry) {
		this.nextIndexEntry = nextIndexEntry;
	}

	@Override
	public LruNode getLruNext() {
		return lruNext;
	}

	@Override
	public void setLruNext(LruNode lruNext) {
		this.lruNext = lruNext;
	}

	@Override
	public LruNode getLruPrev() {
		return lruPrev;
	}

	@Override
	public void setLruPrev(LruNode lruPrev) {
		this.lruPrev = lruPrev;
	}
}
