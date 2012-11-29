package ch.bind.philib.pool.object;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import ch.bind.philib.pool.manager.ObjectManager;

public class NewPool<T> extends PoolBase<T> {

	private final ConcurrentLinkedQueue<T> pool = new ConcurrentLinkedQueue<T>();

	private final AtomicInteger size = new AtomicInteger();

	private final int maxEntries;

	public NewPool(ObjectManager<T> manager, int maxEntries) {
		super(manager);
		this.maxEntries = maxEntries;
	}

	@Override
	protected T tryGetOne() {
		final T value = pool.poll();
		if (value != null) {
			size.decrementAndGet();
			return value;
		}
		return null;
	}

	@Override
	protected boolean tryPutOne(T value) {
		while (true) {
			final int s = size.get();
			if (s >= maxEntries) {
				return false;
			}
			if (size.compareAndSet(s, s + 1)) {
				return pool.offer(value);
			}
		}
	}
}
