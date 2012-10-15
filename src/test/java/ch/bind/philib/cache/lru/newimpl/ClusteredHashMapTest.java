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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ClusteredHashMapTest {
	
	@Test
	public void noDoubleAdds() {
		ClusteredHashMap<Long, String, Entry<Long, String>> map = new ClusteredHashMap<Long, String, Entry<Long, String>>(64);

		for (int i = 0; i < 128; i++) {
			Entry<Long, String> e = new Entry<Long, String>(Long.valueOf(i), Long.toString(i));
			assertTrue(map.add(e));
		}

		for (int i = 0; i < 128; i++) {
			Entry<Long, String> e = new Entry<Long, String>(Long.valueOf(i), Long.toString(i));
			assertFalse(map.add(e), ""+i);
		}
	}

	private static final class Entry<K, V> implements ClusteredHashEntry<K, V> {

		private final K key;
		private final V value;
		private final int hash;

		private ClusteredHashEntry<K, V> next;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
			this.hash = key.hashCode();
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public int cachedHash() {
			return hash;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public ClusteredHashEntry<K, V> getNext() {
			return next;
		}

		@Override
		public void setNext(ClusteredHashEntry<K, V> next) {
			this.next = next;
		}
	}
}
