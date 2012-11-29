package ch.bind.philib.pool.object;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import ch.bind.philib.validation.Validation;

final class LimitedQueue<T> {

	private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<T>();

	private final AtomicInteger size = new AtomicInteger();

	private final int capacity;

	LimitedQueue(int capacity) {
		Validation.isTrue(capacity > 0);
		this.capacity = capacity;
	}

	T poll() {
		final T value = queue.poll();
		if (value != null) {
			size.decrementAndGet();
		}
		return value;
	}

	boolean offer(T value) {
		for (;;) {
			final int s = size.get();
			if (s >= capacity) {
				return false;
			}
			if (size.compareAndSet(s, s + 1)) {
				return queue.offer(value);
			}
		}
	}
}
