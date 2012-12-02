package ch.bind.philib.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import ch.bind.philib.validation.Validation;

public final class LimitedConcurrentQueue<T> {

	private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<T>();

	private final AtomicInteger size = new AtomicInteger();

	private final int capacity;

	public LimitedConcurrentQueue(int limit) {
		Validation.isTrue(limit > 0);
		this.capacity = limit;
	}

	public T poll() {
		final T value = queue.poll();
		if (value != null) {
			size.decrementAndGet();
		}
		return value;
	}

	public boolean offer(T value) {
		for (;;) {
			final int s = size.get();
			if (s >= capacity) {
				return false;
			}
			if (size.compareAndSet(s, s + 1)) {
				return queue.offer(value);
				// NOTE: ConcurrentLinkedQueue always returns true
				// with "correct" implementation would be:
				// if (queue.offer(value)) {return true;}
				// else { size.decrementAndGet(); return false; }
			}
		}
	}

	public int size() {
		return size.get();
	}

	public int getLimit() {
		return capacity;
	}
}
