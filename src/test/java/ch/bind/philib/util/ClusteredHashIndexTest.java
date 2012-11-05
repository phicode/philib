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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import org.testng.annotations.Test;

import ch.bind.philib.util.ClusteredIndex.Entry;

public class ClusteredHashIndexTest {

	@Test
	public void noDoubleAdds() {
		ClusteredHashIndex<Long, TestEntry<Long>> index = new ClusteredHashIndex<Long, TestEntry<Long>>(64);

		for (int i = 0; i < 128; i++) {
			TestEntry<Long> e = new TestEntry<Long>(Long.valueOf(i));
			assertTrue(index.add(e));
		}

		for (int i = 0; i < 128; i++) {
			TestEntry<Long> e = new TestEntry<Long>(Long.valueOf(i));
			assertFalse(index.add(e));
		}
	}

	@Test
	public void addRemove() {
		ClusteredHashIndex<Long, TestEntry<Long>> index = new ClusteredHashIndex<Long, TestEntry<Long>>(64);

		LinkedList<Long> inMap = new LinkedList<Long>();
		for (int i = 0; i < 128; i++) {
			Long key = Long.valueOf(i);
			TestEntry<Long> e = new TestEntry<Long>(key);
			assertTrue(index.add(e));
			inMap.add(key);
		}

		Random rnd = new SecureRandom();
		for (int n = 0; n < 1000; n++) {
			Collections.shuffle(inMap, rnd);

			for (int i = 0; i < 64; i++) {
				Long key = inMap.poll();
				TestEntry<Long> e = index.get(key);
				assertNotNull(e);
				assertTrue(index.remove(e));
			}

			for (int i = -100; i < 200; i++) {
				Long key = Long.valueOf(i);
				TestEntry<Long> e = new TestEntry<Long>(key);
				if (i >= 0 && i < 128) {
					if (inMap.contains(key)) {
						assertFalse(index.add(e));
						assertNotNull(index.get(key));
					} else {
						assertNull(index.get(key));
						assertTrue(index.add(e));
						assertNotNull(index.get(key));
						inMap.add(key);
					}
				} else {
					assertFalse(index.remove(e));
				}
			}
		}
	}

	@Test
	public void sameHashcode() {
		// all keys will land in the exact same position
		// effectively a linked list)

		ClusteredHashIndex<Key, TestEntry<Key>> index = new ClusteredHashIndex<Key, TestEntry<Key>>(64);
		Key[] keys = new Key[1024];
		@SuppressWarnings("unchecked")
		TestEntry<Key>[] entries = new TestEntry[keys.length];
		for (int i = 0, l = keys.length; i < l; i++) {
			Key k = new Key(1);
			TestEntry<Key> e = new TestEntry<Key>(k);
			keys[i] = k;
			entries[i] = e;
			index.add(e);
		}
		assertNull(index.get(new Key(1)));
		for (int i = 0, l = keys.length; i < l; i++) {
			TestEntry<Key> e = entries[i];
			assertTrue(index.get(e.getKey()) == e);
		}
	}

	private static final class TestEntry<K> implements Entry<K> {

		private final K key;

		private Entry<K> nextHashEntry;

		public TestEntry(K key) {
			this.key = key;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public Entry<K> getNextIndexEntry() {
			return nextHashEntry;
		}

		@Override
		public void setNextIndexEntry(Entry<K> nextHashEntry) {
			this.nextHashEntry = nextHashEntry;
		}

		@Override
		public int hashCode() {
			fail();
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			fail();
			return false;
		}
	}

	private static final class Key {

		private final int hash;

		private Key(int hash) {
			super();
			this.hash = hash;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}
	}
}
