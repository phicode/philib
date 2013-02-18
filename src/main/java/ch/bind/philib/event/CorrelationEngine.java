package ch.bind.philib.event;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.bind.philib.util.SimpleTimeoutMap;
import ch.bind.philib.util.TimeoutMap;

public class CorrelationEngine<K, V> {

	private final Lock lock = new ReentrantLock();

	private final Condition added = lock.newCondition();

	private final TimeoutMap<K, V> pending = new SimpleTimeoutMap<K, V>();

	public V add(long timeout, K key, V value) {
		lock.lock();
		try {
			V old = pending.put(timeout, key, value);
			added.signalAll();
			return old;
		} finally {
			lock.unlock();
		}
	}

	public V remove(K key) {
		lock.lock();
		try {
			return pending.remove(key);
		} finally {
			lock.unlock();
		}
	}

	public V blockingPollTimeout() throws InterruptedException {
		lock.lock();
		try {
			while (!Thread.interrupted()) {
				Entry<K, V> entry = pending.pollTimeout();
				if (entry != null) {
					return entry.getValue();
				}
				long nextTimeout = pending.getTimeToNextTimeout();
				nextTimeout = Math.max(1, nextTimeout); // do not use 0

				// wait until the timeout has been reached or a new event added
				if (nextTimeout < Long.MAX_VALUE) {
					added.await(nextTimeout, TimeUnit.MILLISECONDS);
				}
				else {
					added.await();
				}
			}
			Thread.currentThread().interrupt();
			return null;
		} finally {
			lock.unlock();
		}
	}
}
