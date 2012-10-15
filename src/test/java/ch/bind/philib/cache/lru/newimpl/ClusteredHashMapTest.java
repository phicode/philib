package ch.bind.philib.cache.lru.newimpl;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ClusteredHashMapTest {
	@Test
	public void f() {
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
