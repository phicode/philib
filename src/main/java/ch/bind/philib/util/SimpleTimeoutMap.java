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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ch.bind.philib.lang.CompareUtil;
import ch.bind.philib.math.Calc;
import ch.bind.philib.validation.Validation;

/**
 * An implementation of {@link TimeoutMap} which uses the {@link TreeMap} and
 * {@link HashMap} from java.util for internal data management. This
 * implementation is threadsafe.
 * 
 * @author Philipp Meinen
 * 
 * @param <K> type parameter for the map's keys.
 * @param <V> type parameter for the map's values.
 * @see TimeoutMap
 */
public final class SimpleTimeoutMap<K, V> implements TimeoutMap<K, V> {

	private final SortedMap<Long, K> timeoutToKey = new TreeMap<Long, K>();

	private final Map<K, TOEntry<K, V>> keyToValue = new HashMap<K, TOEntry<K, V>>();

	private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

	private final Lock rlock = rwlock.readLock();

	private final Lock wlock = rwlock.writeLock();

	private final Condition putCond = wlock.newCondition();

	@Override
	public V put(long timeout, K key, V value) {
		Validation.notNegative(timeout, "timeout must not be negative");
		long timestampNs = System.nanoTime() + (timeout * 1000000L);
		return _putWithTimestampNs(timestampNs, key, value);
	}

	private V _putWithTimestampNs(final long timestampNs, final K key, final V value) {
		Validation.notNull(key, "key must not be null");
		Validation.notNull(value, "value must not be null");
		wlock.lock();
		try {
			TOEntry<K, V> previous = keyToValue.remove(key);
			if (previous != null) {
				timeoutToKey.remove(previous.timestampNs);
			}

			long actualTimestampNs = timestampNs;
			// prevent duplicate timestamps, which should be rather rare due to
			// the nanosecond resolution
			while (timeoutToKey.containsKey(actualTimestampNs)) {
				// add a random 1us jitter so that we get some spread when many
				// key-value pairs are added in short succession
				actualTimestampNs += ((long) (Math.random() * 1000) + 1);
			}
			TOEntry<K, V> entry = new TOEntry<K, V>(actualTimestampNs, key, value);
			timeoutToKey.put(actualTimestampNs, key);
			keyToValue.put(key, entry);
			putCond.signalAll();
			return previous == null ? null : previous.value;
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public V get(K key) {
		rlock.lock();
		try {
			TOEntry<K, V> entry = keyToValue.get(key);
			if (entry != null) {
				return entry.value;
			}
			return null;
		} finally {
			rlock.unlock();
		}
	}

	@Override
	public V remove(K key) {
		wlock.lock();
		try {
			TOEntry<K, V> entry = keyToValue.remove(key);
			if (entry != null) {
				long timestampNs = entry.timestampNs;
				K otherKey = timeoutToKey.remove(timestampNs);
				assert (otherKey != null);
				return entry.value;
			}
			return null;
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public Map.Entry<K, V> pollTimeoutNow() {
		wlock.lock();
		try {
			return _pollTimedoutNs(System.nanoTime());
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public Map.Entry<K, V> pollTimeoutBlocking() throws InterruptedException {
		return pollTimeoutBlocking(0, TimeUnit.NANOSECONDS);
	}

	@Override
	public Map.Entry<K, V> pollTimeoutBlocking(long duration, TimeUnit timeUnit) throws InterruptedException {
		Validation.notNull(timeUnit);
		final long untilNs = duration == 0 ? 0 : System.nanoTime() + timeUnit.toNanos(duration);
		wlock.lock();
		try {
			final Thread t = Thread.currentThread();
			while (!t.isInterrupted()) {
				long nowNs = System.nanoTime();
				Entry<K, V> entry = _pollTimedoutNs(nowNs);
				if (entry != null) {
					return entry;
				}
				// never 0 since we reuse nowNs
				long nextTimeoutNs = _getTimeToNextTimeoutNs(nowNs);

				// wait until the timeout has been reached or a new event added
				if (untilNs == 0) {
					if (nextTimeoutNs == Long.MAX_VALUE) {
						putCond.await();
					}
					else {
						putCond.await(nextTimeoutNs, TimeUnit.NANOSECONDS);
					}
				}
				else {
					long awaitTimeoutNs = untilNs - nowNs;
					if (awaitTimeoutNs < 1) {
						return null;
					}
					awaitTimeoutNs = Math.min(awaitTimeoutNs, nextTimeoutNs);
					putCond.await(awaitTimeoutNs, TimeUnit.NANOSECONDS);
				}
			}
			throw new InterruptedException();
		} finally {
			wlock.unlock();
		}
	}

	/**
	 * Find entries that are timed out
	 * 
	 * @param timestamp The timeout cap. Anything older than that is timed out
	 * @return the oldest timed out entry
	 */
	private Map.Entry<K, V> _pollTimedoutNs(final long timestampNs) {
		if (timeoutToKey.isEmpty()) {
			return null;
		}
		Long lowestNs = timeoutToKey.firstKey();
		if (timestampNs >= lowestNs) {
			K key = timeoutToKey.remove(lowestNs);
			return keyToValue.remove(key);
		}
		// no entry timed out, or no entries at all
		return null;
	}

	@Override
	public void clear() {
		wlock.lock();
		try {
			timeoutToKey.clear();
			keyToValue.clear();
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public int size() {
		rlock.lock();
		try {
			return keyToValue.size();
		} finally {
			rlock.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		rlock.lock();
		try {
			return keyToValue.isEmpty();
		} finally {
			rlock.unlock();
		}
	}

	@Override
	public boolean containsKey(K key) {
		rlock.lock();
		try {
			return keyToValue.containsKey(key);
		} finally {
			rlock.unlock();
		}
	}

	@Override
	public long getTimeToNextTimeout() {
		rlock.lock();
		try {
			long nowNs = System.nanoTime();
			long tNs = _getTimeToNextTimeoutNs(nowNs);
			if (tNs == 0) {
				return 0;
			}
			else if (tNs == Long.MAX_VALUE) {
				return Long.MAX_VALUE;
			}
			else {
				return Calc.ceilDiv(tNs, 1000000L);
			}
		} finally {
			rlock.unlock();
		}
	}

	public long _getTimeToNextTimeoutNs(long nowNs) {
		if (timeoutToKey.isEmpty()) {
			return Long.MAX_VALUE;
		}
		Long lowest = timeoutToKey.firstKey();
		long diff = lowest.longValue() - nowNs;
		return diff < 0 ? 0 : diff;
	}

	static final class TOEntry<K, V> implements Map.Entry<K, V> {

		final long timestampNs;

		final K key;

		final V value;

		TOEntry(long timestampNs, K key, V value) {
			this.timestampNs = timestampNs;
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException("setValue is not supported for entries of a TimeoutMap");
		}

		@Override
		public boolean equals(Object obj) {
			// according to the contract implied by Map.Entry
			// but without the null checks since this implementation does not
			// allow for null keys or values
			if (obj == this) {
				return true;
			}
			if (obj instanceof Map.Entry) {
				Map.Entry other = (Map.Entry) obj;
				return CompareUtil.equals(this.key, other.getKey()) && //
						CompareUtil.equals(this.value, other.getValue());
			}
			return false;
		}

		@Override
		public int hashCode() {
			// according to the contract implied by Map.Entry
			// but without the null checks since this implementation does not
			// allow for null keys or values
			return key.hashCode() ^ value.hashCode();
		}
	}
}
