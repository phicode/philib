/**
 * Copyright: Copyright (c) 2010-2011 Siemens IT Solutions and Services AG
 *
 * $HeadURL: https://svn2.fit.ch001.ch:8443/svn/polyalert/trunk/02_Los_1/02_Production/polyalert-cc/src/server/com/com-common/src/main/java/ch/admin/babs/polyalert/server/com/common/util/JavaUtilTimeoutMap.java $
 *
 * $LastChangedDate: 2012-09-26 19:04:40 +0200 (Wed, 26 Sep 2012) $
 * $LastChangedBy: chamehl0 $
 * $LastChangedRevision: 30552 $
 */
package ch.bind.philib.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ch.bind.philib.lang.CompareUtil;
import ch.bind.philib.validation.Validation;

/**
 * An implementation of {@link TimeoutMap} which uses the {@link TreeMap} and
 * {@link HashMap} from java.util for internal data management. This
 * implementation is threadsafe.
 * 
 * @author Philipp Meinen
 * 
 * @param <K>
 *            type parameter for the map's keys.
 * @param <V>
 *            type parameter for the map's values.
 * @see TimeoutMap
 */
public final class JavaUtilTimeoutMap<K, V> implements TimeoutMap<K, V> {

	private final SortedMap<Long, K> timeoutToKey = new TreeMap<Long, K>();

	private final Map<K, TOEntry<K, V>> keyToValue = new HashMap<K, TOEntry<K, V>>();

	private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

	private final Lock rlock = rwlock.readLock();

	private final Lock wlock = rwlock.writeLock();

	@Override
	public void add(long timeout, TimeUnit timeUnit, K key, V value) {
		Validation.notNegative(timeout, "timeout must not be negative");
		Validation.notNull(timeUnit, "time-unit must not be null");
		long nowNs = System.nanoTime();
		long timeoutNs = timeUnit.toNanos(timeout);
		long timestampNs = nowNs + timeoutNs;
		_addWithTimestampNs(timestampNs, key, value);
	}

	@Override
	public void addWithTimestamp(long timestamp, TimeUnit timeUnit, K key, V value) {
		Validation.notNegative(timestamp, "timestamp must not be negative");
		Validation.notNull(timeUnit, "time-unit must not be null");
		long timestampNs = timeUnit.toNanos(timestamp);
		_addWithTimestampNs(timestampNs, key, value);
	}

	private void _addWithTimestampNs(final long timestampNs, final K key, final V value) {
		Validation.notNull(key, "key must not be null");
		Validation.notNull(value, "value must not be null");
		wlock.lock();
		try {
			TOEntry<K, V> existing = keyToValue.remove(key);
			if (existing != null) {
				timeoutToKey.remove(existing.timestampNs);
			}

			long actualTimestampNs = timestampNs;
			// prevent duplicate timestamps, which should be rather rare due to
			// the nanosecond resolution
			while (timeoutToKey.get(actualTimestampNs) != null) {
				// add a random number somewhere between so that we get some
				// spread when many key-value pairs are added in short
				// succession
				actualTimestampNs += (long) (Math.random() * 25000);
			}
			TOEntry<K, V> entry = new TOEntry<K, V>(actualTimestampNs, key, value);
			timeoutToKey.put(actualTimestampNs, key);
			keyToValue.put(key, entry);
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
	public Map.Entry<K, V> pollTimeout() {
		wlock.lock();
		try {
			long nowNs = System.nanoTime();
			return _pollTimedoutNs(nowNs);
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public Map.Entry<K, V> pollTimeout(long timestamp, TimeUnit timeUnit) {
		Validation.notNegative(timestamp);
		Validation.notNull(timeUnit);
		wlock.lock();
		try {
			return _pollTimedoutNs(timeUnit.toNanos(timestamp));
		} finally {
			wlock.unlock();
		}
	}

	/**
	 * Find entries that are timed out
	 * 
	 * @param timestamp
	 *            The timeout cap. Anything older than that is timed out
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
	public long getTimeToNextTimeout(TimeUnit timeUnit) {
		Validation.notNull(timeUnit);
		rlock.lock();
		try {
			if (timeoutToKey.isEmpty()) {
				return Long.MAX_VALUE;
			}
			Long lowest = timeoutToKey.firstKey();
			long nowNs = System.nanoTime();
			long diff = lowest.longValue() - nowNs;
			return diff < 0 ? 0 : timeUnit.convert(diff, TimeUnit.NANOSECONDS);
		} finally {
			rlock.unlock();
		}
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
