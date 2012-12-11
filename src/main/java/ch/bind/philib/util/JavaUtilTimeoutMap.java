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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
public final class JavaUtilTimeoutMap<K, V> implements TimeoutMap<K, V> {

	private final SortedMap<Long, K> timeoutToKey = new TreeMap<Long, K>();

	private final Map<K, Entry<K, V>> keyToValue = new HashMap<K, Entry<K, V>>();

	private final Lock lock = new ReentrantLock();

	@Override
	public void addWithRange(long timeout, TimeUnit timeUnit, K key, V value) {
		Validation.notNegative(timeout, "timeout must be >= 0");
		Validation.notNull(timeUnit);
		long nowNs = System.nanoTime();
		long timeoutNs = timeUnit.toNanos(timeout);
		long timestampNs = nowNs + timeoutNs;
		_addWithTimestampNs(timestampNs, key, value);
	}

	@Override
	public void addWithTimestamp(long timestamp, TimeUnit timeUnit, K key, V value) {
		Validation.notNegative(timestamp, "timestamp must be >= 0");
		Validation.notNull(timeUnit);
		long timestampNs = timeUnit.toNanos(timestamp);
		_addWithTimestampNs(timestampNs, key, value);
	}

	private void _addWithTimestampNs(long timestampNs, final K key, final V value) {
		Validation.notNull(key, "key must not be null");
		Validation.notNull(value, "value must not be null");
		lock.lock();
		try {
			// TODO: treat duplicate keys as overwrite operations
			if (keyToValue.containsKey(key)) {
				throw new IllegalArgumentException("can not add duplicate key: " + key);
			}
			// prevent duplicate timestamps, which should be rather rare due to
			// the nanosecond resolution
			while (timeoutToKey.get(timestampNs) != null) {
				// TODO: add a random number somewhere between 1 and 50000 ns
				timestampNs++;
			}
			Entry<K, V> entry = new Entry<K, V>(timestampNs, key, value);
			timeoutToKey.put(timestampNs, key);
			keyToValue.put(key, entry);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V get(K key) {
		lock.lock();
		try {
			TOEntry<K, V> entry = keyToValue.get(key);
			if (entry != null) {
				return entry.getValue();
			}
			return null;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public TOEntry<K, V> remove(K key) {
		lock.lock();
		try {
			TOEntry<K, V> entry = keyToValue.remove(key);
			if (entry != null) {
				long timoutAt = entry.getTimeoutAt();
				List<K> keys = timeoutToKeys.get(timoutAt);
				keys.remove(key);
				if (keys.isEmpty()) {
					// no more entries for this timeout-slot
					timeoutToKeys.remove(timoutAt);
				}
			}
			return entry;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public TOEntry<K, V> pollTimeout() {
		lock.lock();
		try {
			long now = System.nanoTime();
			return _pollTimedout(now, TimeUnit.NANOSECONDS);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public TOEntry<K, V> pollTimeout(long time, TimeUnit timeUnit) {
		Validation.notNegative(time);
		Validation.notNull(timeUnit);
		lock.lock();
		try {
			return pollTimedout(time);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Find entries that are timed out
	 * @param time The timeout cap. Anything older than that is timed out
	 * @return the oldest timed out entry
	 */
	private TOEntry<K, V> _pollTimedout(long time, TimeUnit timeUnit) {
		if (timeoutToKeys.isEmpty()) {
			return null;
		}
		Long lowest = timeoutToKeys.firstKey();
		if (time >= lowest) {
			List<K> keys = timeoutToKeys.get(lowest);
			K key = keys.remove(0);
			if (keys.isEmpty()) {
				// no more entries for this timeout-slot
				timeoutToKeys.remove(lowest);
			}
			return keyToValue.remove(key);
		}
		// no entry timed out, or no entries at all
		return null;
	}

	@Override
	public void clear() {
		lock.lock();
		try {
			timeoutToKeys.clear();
			keyToValue.clear();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int size() {
		lock.lock();
		try {
			return keyToValue.size();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		lock.lock();
		try {
			return keyToValue.isEmpty();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean containsKey(K key) {
		lock.lock();
		try {
			return keyToValue.containsKey(key);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long getTimeToNextTimeoutMs() {
		lock.lock();
		try {
			if (timeoutToKeys.isEmpty()) {
				return 0;
			}
			Long lowest = timeoutToKeys.firstKey();
			long now = System.currentTimeMillis();
			long diff = lowest.longValue() - now;
			return diff < 0 ? 0 : diff;
		} finally {
			lock.unlock();
		}
	}

	static final class Entry<K, V> {
		final long timestampNs;

		final K key;

		final V value;

		public Entry(long timestampNs, K key, V value) {
			this.timestampNs = timestampNs;
			this.key = key;
			this.value = value;
		}
	}

}
