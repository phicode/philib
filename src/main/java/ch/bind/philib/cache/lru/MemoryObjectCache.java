/*
 * Copyright (c) 2006 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.cache.lru;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A <code>MemoryObjectCache<code> caches Key-Value-Pairs
 * in the system memory.
 * There are two values to control the behavior of a Cache:
 * <ul>
 * <li>
 * The <code>timeout</code> specifies the maximum time in <b>milliseconds</b>.
 * If a Key-Value-Pair gets older than this value it will be removed
 * automatically. </li> <li>
 * The <code>capacity</code> specifies the maximum number of Key-Value-Pairs
 * that may be stored in a <code>MemoryObjectCache</code>. The cache maintains
 * an internal list to know which Pair was not accessed for the longest time. If
 * one wants to add a Pair to an already full cache the Pair which was not
 * accessed for the longest time will be removed to make room for the new pair.</li>
 * </ul>
 * 
 * @param <K>
 *            The type which is used as a key into the cache. If serialization
 *            is performed on a cache its key-type must implement the
 *            {@link java.io.Serializable} interface.
 * @param <V>
 *            The type which is used as a value for the cache. If serialization
 *            is performed on a cache its value-type must implement the
 *            {@link java.io.Serializable} interface.
 * 
 * @author Philipp Meinen
 */
// TODO: synchronization within buckets to improve parallel threaded acess
public final class MemoryObjectCache<K, V> implements ICache<K, V>, Serializable {

	/** The minumum capacity of an object cache. */
	public static final int MIN_CACHE_CAPACITY = 64;

	/** The default capacity of an object cache. */
	public static final int DEFAULT_CACHE_CAPACITY = 1024;

	/**
	 * The default timeout of an object cache is zero which is equal to no
	 * timeout.
	 */
	public final static long DEFAULT_TIMEOUT = 0;

	private static final long serialVersionUID = 1126656384821894976L;

	private long timeout;
	private int capacity;
	private int size;

	private Pair<K, V>[] hashTable;

	private Pair<K, V> lruYoungest; // most recently accessed
	private Pair<K, V> lruOldest; // least recently accessed

	private Pair<K, V> timeYoungest; // newest pair
	private Pair<K, V> timeOldest; // oldest pair

	private ArrayList<RecycleListener<K, V>> recycleListeners;

	/**
	 * Creates a <code>MemoryObjectCache</code> with the capacity
	 * {@link #DEFAULT_CACHE_CAPACITY} and the timeout {@link #DEFAULT_TIMEOUT}.
	 * 
	 * @see #MemoryObjectCache(int, long)
	 */
	public MemoryObjectCache() {
		init(DEFAULT_CACHE_CAPACITY, DEFAULT_TIMEOUT);
	}

	/**
	 * Creates a <code>MemoryObjectCache</code> with a specified
	 * <code>capacity</code> and the timeout {@link #DEFAULT_TIMEOUT}.
	 * 
	 * @see #MemoryObjectCache(int, long)
	 */
	public MemoryObjectCache(int capacity) {
		init(capacity, DEFAULT_TIMEOUT);
	}

	/**
	 * Creates a <code>MemoryObjectCache</code> with a specified
	 * <code>capacity</code> and a specified <code>timeout</code> in
	 * milliseconds.
	 * 
	 * @param capacity
	 *            the maximum capacity of the newly created
	 *            <code>MemoryObjectCache</code>. This value should not be to
	 *            low.
	 * @param timeout
	 *            the maximum time in milliseconds that each pair should be hold
	 *            in the cache.
	 * @throws IllegalArgumentException
	 *             if the <code>timeout</code> is less than 0.
	 */
	public MemoryObjectCache(int capacity, long timeout) {
		init(capacity, timeout);
	}

	/**
	 * @see ICache#add(Object, Object)
	 */
	@Override
	public void add(K key, V value) {
		if (key == null)
			throw new IllegalArgumentException("the key must not be null");

		final long timeNow = getTimeNow();
		final long pairTimeout = timeNow + timeout;

		Pair<K, V> pair = HashTable_get(key, timeNow);
		if (pair == null) { // new pair
			pair = new Pair<K, V>(key, value, pairTimeout);
			if (size >= capacity) { // make some room
				// use timed out pairs if such exist
				if (timeout != 0 && timeOldest.timeout <= timeNow) {
					_remove(timeOldest);
				} else {
					// otherwise remove the element which
					// was not accessed the longest
					_remove(lruOldest);
				}
			}
			HashTable_add(pair);
			LRU_add(pair);
			TimeOutList_add(pair);
			size++;
		} else { // existing pair
			// TODO: document, that the key is not replaced -> old object
			pair.update(value, pairTimeout);
			LRU_toYoungest(pair);
			TimeOutList_toYoungest(pair);
		}
	}

	/**
	 * @see ICache#get(Object)
	 */
	@Override
	public V get(K key) {
		if (key == null)
			throw new IllegalArgumentException("the key must not be null");
		final long timeNow = getTimeNow();
		Pair<K, V> pair = HashTable_get(key, timeNow);
		if (pair != null) {
			LRU_toYoungest(pair);
			return pair.value;
		} else {
			return null;
		}
	}

	/**
	 * @see ICache#remove(Object)
	 */
	@Override
	public void remove(K key) {
		if (key == null)
			throw new IllegalArgumentException("the key must not be null");
		Pair<K, V> pair = HashTable_get(key, getTimeNow());
		if (pair != null)
			_remove(pair);
	}

	/**
	 * @see ICache#contains(Object)
	 */
	@Override
	public boolean contains(K key) {
		if (key == null)
			throw new IllegalArgumentException("the key must not be null");
		Pair<K, V> pair = HashTable_get(key, getTimeNow());
		return pair != null;
	}

	/**
	 * @see ICache#size()
	 */
	@Override
	public int size() {
		clearTimedOutPairs();
		return size;
	}

	/**
	 * @see ICache#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		clearTimedOutPairs();
		return size == 0;
	}

	/**
	 * @see ICache#getTimeout()
	 */
	@Override
	public long getTimeout() {
		return timeout;
	}

	/**
	 * @see ICache#getCapacity()
	 */
	@Override
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @see ICache#clear()
	 */
	@Override
	public void clear() {
		if (size == 0)
			return;
		for (int i = 0; i < capacity; i++)
			hashTable[i] = null;
		Pair<K, V> current = lruOldest;
		do {
			Pair<K, V> next = lruOldest.lruYounger;
			current.nextInHash = null;
			current.lruOlder = null;
			current.lruYounger = null;
			current.value = null;
			current = next;
		} while (current != null);
		lruOldest = null;
		lruYoungest = null;

		size = 0;
	}

	@Override
	public void clearTimedOutPairs() {
		// return immediately if we are not using a timeout
		if (timeout == 0)
			return;
		final long timeNow = getTimeNow();
		while (timeOldest != null && timeOldest.timeout < timeNow)
			_remove(timeOldest);
	}

	@Override
	public void addRecycleListener(RecycleListener<K, V> listener) {
		if (listener != null) {
			if (recycleListeners == null)
				recycleListeners = new ArrayList<RecycleListener<K, V>>();
			if (!recycleListeners.contains(listener))
				recycleListeners.add(listener);
		}
	}

	@Override
	public void removeRecycleListener(RecycleListener<K, V> listener) {
		if (listener == null || recycleListeners == null)
			return;
		recycleListeners.remove(listener);
		if (recycleListeners.size() < 1)
			recycleListeners = null;
	}

	// For debugging purposes only
	public int getMostPackedHashEntry() {
		int most = 0;
		for (int i = 0; i < capacity; i++) {
			int current = 0;
			Pair<K, V> pair = hashTable[i];
			while (pair != null) {
				current++;
				pair = pair.nextInHash;
			}
			if (current > most) {
				most = current;
			}
		}
		return most;
	}

	// ///////////////////
	// private methods //
	// ///////////////////

	private long getTimeNow() {
		return (timeout == 0) ? 0 : System.currentTimeMillis();
	}

	@SuppressWarnings("unchecked")
	private void init(int capacity, long timeout) {
		if (capacity < MIN_CACHE_CAPACITY)
			capacity = MIN_CACHE_CAPACITY;
		if (timeout < 0)
			throw new IllegalArgumentException("the timeout of the cache must not be less than zero");
		this.hashTable = new Pair[capacity];
		this.capacity = capacity;
		this.lruYoungest = null;
		this.lruOldest = null;
		this.timeout = timeout;
		this.recycleListeners = null;
	}

	private void _remove(Pair<K, V> pair) {
		HashTable_remove(pair);
		LRU_remove(pair);
		TimeOutList_remove(pair);
		size--;
		fireRecycleListener(pair);
	}

	private void fireRecycleListener(Pair<K, V> p) {
		if (recycleListeners != null) {
			final int n = recycleListeners.size();
			for (int i = 0; i < n; i++) {
				RecycleListener<K, V> rl = recycleListeners.get(i);
				rl.onRecyclePair(p.key, p.value);
			}
		}
	}

	private void HashTable_add(Pair<K, V> pair) {
		final int hash = pair.hash;
		final int position = hashPosition(hash);
		Pair<K, V> hashtablePair = hashTable[position];
		if (hashtablePair == null) {
			hashTable[position] = pair;
		} else {
			while (hashtablePair.nextInHash != null) {
				hashtablePair = hashtablePair.nextInHash;
			}
			hashtablePair.nextInHash = pair;
		}
	}

	private void HashTable_remove(Pair<K, V> pair) {
		final K key = pair.key;
		final int hash = pair.hashCode();
		final int position = hashPosition(hash);
		Pair<K, V> previous = null;
		Pair<K, V> current = hashTable[position];
		while (current != null && current.hash != hash && current.key.equals(key) == false) {
			previous = current;
			current = current.nextInHash;
		}
		if (current != null) {
			if (previous == null) {
				hashTable[position] = current.nextInHash;
			} else {
				previous.nextInHash = current.nextInHash;
			}
			// lets be nice to the gc
			current.nextInHash = null;
		}
	}

	// returns null if a pair does not exist.
	// otherwise the object will be returned but the LRU wont be updated
	private Pair<K, V> HashTable_get(K key, long timeNow) {
		final int hash = key.hashCode();
		final int position = hashPosition(hash);
		Pair<K, V> pair = hashTable[position];
		while (pair != null && pair.hash != hash && pair.key.equals(key) == false)
			pair = pair.nextInHash;
		if (pair != null) {
			if (timeNow == 0) {
				return pair;
			} else {
				if (pair.timeout > timeNow) {
					return pair;
				} else {
					_remove(pair);
					return null;
				}
			}
		} else {
			return null;
		}
	}

	private void LRU_add(Pair<K, V> pair) {
		if (size > 0) {
			Pair<K, V> oldYoungest = lruYoungest;
			pair.lruOlder = oldYoungest;
			oldYoungest.lruYounger = pair;
		} else {
			lruOldest = pair;
		}
		pair.lruYounger = null;
		lruYoungest = pair;
	}

	private void LRU_remove(Pair<K, V> pair) {
		if (size == 1) {
			lruOldest = null;
			lruYoungest = null;
		} else {
			if (pair == lruOldest) { // end
				lruOldest = pair.lruYounger;
				lruOldest.lruOlder = null;
			} else if (pair == lruYoungest) { // front
				lruYoungest = pair.lruOlder;
				lruYoungest.lruYounger = null;
			} else { // in the middle
				Pair<K, V> next = pair.lruYounger;
				Pair<K, V> prev = pair.lruOlder;

				prev.lruYounger = next;
				next.lruOlder = prev;
			}
			// lets be nice to the gc
			pair.lruOlder = null;
			pair.lruYounger = null;
		}
	}

	private void LRU_toYoungest(Pair<K, V> pair) { // TODO: review again
		// nothing to do if we only have one item
		// or the pair is the youngest already
		if (size == 1 || pair == lruYoungest)
			return;

		if (pair != lruOldest) {
			// remove from the old position
			Pair<K, V> younger = pair.lruYounger;
			Pair<K, V> older = pair.lruOlder;
			younger.lruOlder = older;
			older.lruYounger = younger;
		} else { // was oldest-element
			Pair<K, V> nowOldest = pair.lruYounger;
			nowOldest.lruOlder = null;
			lruOldest = nowOldest;
		}
		lruYoungest.lruYounger = pair;
		pair.lruOlder = lruYoungest;
		lruYoungest = pair;
		pair.lruYounger = null;
	}

	private void TimeOutList_add(Pair<K, V> pair) {
		if (timeout == 0)
			return;
		if (size != 0) {
			Pair<K, V> prev = timeYoungest;
			pair.timeoutOlder = prev;
			prev.timeoutYounger = pair;
		} else {
			timeOldest = pair;
		}
		timeYoungest = pair;
	}

	private void TimeOutList_remove(Pair<K, V> pair) {
		if (timeout == 0) {
			return;
		}
		if (size == 1) {
			timeOldest = null;
			timeYoungest = null;
		} else {
			if (pair == timeOldest) { // end
				timeOldest = pair.timeoutYounger;
				timeOldest.timeoutOlder = null;
			} else if (pair == timeYoungest) { // front
				timeYoungest = pair.timeoutOlder;
				timeYoungest.timeoutYounger = null;
			} else { // in the middle
				Pair<K, V> next = pair.timeoutYounger;
				Pair<K, V> prev = pair.timeoutOlder;

				prev.timeoutYounger = next;
				next.timeoutOlder = prev;
			}
			// lets be nice to the gc
			pair.timeoutYounger = null;
			pair.timeoutOlder = null;
		}
	}

	private void TimeOutList_toYoungest(Pair<K, V> pair) { // TODO: review again
		// nothing to do if we only have one item
		// or the pair is the youngest already
		if (timeout == 0 || size == 1 || pair == timeYoungest)
			return;

		if (pair != timeOldest) {
			// remove from the old position
			Pair<K, V> younger = pair.timeoutYounger;
			Pair<K, V> older = pair.timeoutOlder;
			younger.timeoutOlder = older;
			older.timeoutYounger = younger;
		} else { // was oldest-element
			Pair<K, V> nowOldest = pair.timeoutYounger;
			nowOldest.timeoutOlder = null;
			timeOldest = nowOldest;
		}
		timeOldest.timeoutYounger = pair;
		pair.timeoutOlder = timeYoungest;
		timeYoungest = pair;
		pair.timeoutYounger = null;
	}

	private int hashPosition(int hash) {
		int p = hash % capacity;
		return (p >= 0) ? p : -p;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(capacity);
		out.writeLong(timeout);
		out.writeInt(size);
		// start with the oldest from the lru
		// because when reading the data back we will start
		// with this oldest element and it will become
		// the oldest in the new cache.
		Pair<K, V> pair = lruOldest;
		while (pair != null) {
			out.writeObject(pair.key);
			out.writeObject(pair.value);
			out.writeLong(pair.timeout);
			pair = pair.lruYounger;
		}
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		final long timeNow = System.currentTimeMillis();
		int capacity = in.readInt();
		long timeout = in.readLong();
		init(capacity, timeout);
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			K key = (K) in.readObject();
			V value = (V) in.readObject();
			long pairTimeout = in.readLong();
			if (timeout == 0 || pairTimeout > timeNow)
				add(key, value);
		}
	}

	private static final class Pair<K, V> {

		private Pair(K key, V value, long timeout) {
			this.key = key;
			this.value = value;
			this.timeout = timeout;
			hash = key.hashCode();
		}

		private final K key;
		private final int hash;

		private volatile V value;
		private long timeout;

		Pair<K, V> nextInHash;

		Pair<K, V> lruYounger;
		Pair<K, V> lruOlder;

		Pair<K, V> timeoutYounger;
		Pair<K, V> timeoutOlder;


		public void update(V value, long timeout) {
			this.value = value;
			this.timeout = timeout;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}
	}
}