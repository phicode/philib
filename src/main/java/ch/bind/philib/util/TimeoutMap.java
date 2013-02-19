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

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A map which additionaly to normal map operations allows the user to supply a
 * timeout. The oldest timed-out entry in the map can be found through two
 * {@link TimeoutMap#findTimedout} methods.
 * 
 * @author Philipp Meinen
 * 
 * @param <K> type parameter for the map's keys.
 * @param <V> type parameter for the map's values.
 */
public interface TimeoutMap<K, V> {

	/**
	 * Add a key-value pair with an associated timeout to the map. The resulting
	 * timeout-timestamp is the current time plus the supplied timeout.
	 * 
	 * @param timeout the timeout for this key-value pair (in milliseconds)
	 * @param key -
	 * @param value -
	 * @return the value which was previously associated with the given key or
	 *         {@code null} if none.
	 */
	V put(long timeout, K key, V value);

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
	 * @param key the key for which an entry must be removed.
	 * @return {@code null} if there was no entry for this key in that map,
	 *         otherwise the value of the removed entry.
	 */
	V remove(K key);

	/**
	 * <b>Nonblocking</b> poll for the next entry which is timed out, based on
	 * the time of the invocation of this method.
	 * 
	 * @return {@code null} if there is no timed-out entry in this map.
	 *         otherwise the oldest timed-out entry.
	 */
	Map.Entry<K, V> pollTimeoutNow();

	/**
	 * <b>Blocking</b> poll for the next entry which is timed out.
	 * 
	 * @return {@code null} if there is no timed-out entry in this map within
	 *         the given duration. otherwise the oldest timed-out entry.
	 */
	Map.Entry<K, V> pollTimeoutBlocking() throws InterruptedException;
	
	/**
	 * <b>Blocking</b> poll for the next entry which is timed out. The
	 * parameters duration and timeUnit define for how long the method must wait
	 * for a timeout to occur.
	 * 
	 * @return {@code null} if there is no timed-out entry in this map within
	 *         the given duration. otherwise the oldest timed-out entry.
	 */
	Map.Entry<K, V> pollTimeoutBlocking(long duration, TimeUnit timeUnit) throws InterruptedException;

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
	 * @param key -
	 * @return {@code true} if there is an entry identified by that key in this
	 *         map, {@code false otherwise}.
	 */
	boolean containsKey(K key);

	/**
	 * @return {@link Long.MAX_VALUE} if the TimeoutMap is empty. Otherwise the
	 *         time until the next entry times out (in milliseconds since the
	 *         epoch). A return value of zero indicates that there is at least
	 *         one entry which has already timed out.
	 */
	long getTimeToNextTimeout();
}
