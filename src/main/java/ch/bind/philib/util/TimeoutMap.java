/**
 * Copyright: Copyright (c) 2010-2011 Siemens IT Solutions and Services AG
 *
 * $HeadURL: https://svn2.fit.ch001.ch:8443/svn/polyalert/trunk/02_Los_1/02_Production/polyalert-cc/src/server/com/com-common/src/main/java/ch/admin/babs/polyalert/server/com/common/util/TimeoutMap.java $
 *
 * $LastChangedDate: 2012-09-26 19:04:40 +0200 (Wed, 26 Sep 2012) $
 * $LastChangedBy: chamehl0 $
 * $LastChangedRevision: 30552 $
 */
package ch.bind.philib.util;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A map which additionaly to normal map operations allows the user to supply a
 * timeout. The oldest timed-out entry in the map can be found through two
 * {@link TimeoutMap#findTimedout} methods.
 * 
 * @author Philipp Meinen
 * 
 * @param <K>
 *            type parameter for the map's keys.
 * @param <V>
 *            type parameter for the map's values.
 */
public interface TimeoutMap<K, V> {

	/**
	 * Add a key-value pair with an associated timeout to the map. The resulting
	 * timeout-timestamp is the current time plus the supplied timeout.
	 * 
	 * @param timeout
	 *            the timeout.for this key-value pair
	 * @param timeUnit
	 *            the unit of the parameter timeout
	 * @param key
	 *            -
	 * @param value
	 *            -
	 */
	void add(long timeout, TimeUnit timeUnit, K key, V value);

	/**
	 * Add a key-value pair with a specific timeout timestamp.
	 * 
	 * @param timestamp
	 *            the timestamp when this key-value pair will expire
	 * @param timeUnit
	 *            the unit of the parameter timestamp
	 * @param key
	 *            -
	 * @param value
	 *            -
	 */
	void addWithTimestamp(long timestamp, TimeUnit timeUnit, K key, V value);

	/**
	 * Searches a value by its key
	 * 
	 * @param key
	 * @return {@code null} if the key does not exist.
	 */
	V get(K key);

	/**
	 * removes an entry by it's key
	 * 
	 * @param key
	 *            the key for which an entry must be removed.
	 * @return {@code null} if there was no entry for this key in that map,
	 *         otherwise the value of the removed entry.
	 */
	V remove(K key);

	/**
	 * finds the next entry which is timed out, based on the time of the
	 * invocation of this method.
	 * 
	 * @return {@code null} if there is no timed-out entry in this map.
	 *         otherwise the oldest timed-out entry.
	 */
	Map.Entry<K, V> pollTimeout();

	/**
	 * Finds the next entry which is timed out, based on a supplied timestamp.
	 * 
	 * @param timestamp
	 *            a supplied timestamp, for finding the next timed out entry
	 * @param timeUnit
	 *            the unit of the parameter timestamp.
	 * @return {@code null} if there is no timed-out entry in this map.
	 *         otherwise the oldest timed-out entry.
	 * @throws IllegalArgumentException
	 *             if the time is negative or the timeUnit is null
	 */
	Map.Entry<K, V> pollTimeout(long timestamp, TimeUnit timeUnit);

	/**
	 * removes all key-value pairs from this map.
	 */
	void clear();

	/**
	 * query the number of entries in this map.
	 * 
	 * @return the number of entries in this map.
	 */
	int size();

	/**
	 * @return {@code true} if this map is empty, {@code false} otherwise.
	 */
	boolean isEmpty();

	/**
	 * Check for the existence of a key.
	 * 
	 * @param key
	 *            -
	 * @return {@code true} if there is an entry identified by that key in this
	 *         map, {@code false otherwise}.
	 */
	boolean containsKey(K key);

	/**
	 * @param timeUnit
	 *            the unit of the returned value.
	 * @return {@link Long.MAX_VALUE} if the TimeoutMap is empty. Otherwise the
	 *         time until the next entry times out. A return value of zero
	 *         indicates that there is at least one entry which has already
	 *         timed out.
	 */
	long getTimeToNextTimeout(TimeUnit timeUnit);
}
