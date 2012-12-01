package ch.bind.philib.pool.object;

import java.lang.ref.SoftReference;

import ch.bind.philib.pool.manager.ObjectManager;
import ch.bind.philib.util.LimitedConcurrentQueue;

public final class LeakyPool<T> extends PoolBase<T> {

	private final LimitedConcurrentQueue<SoftReference<T>> queue;

	public LeakyPool(ObjectManager<T> manager, int maxEntries) {
		super(manager);
		queue = new LimitedConcurrentQueue<SoftReference<T>>(maxEntries);
	}

	@Override
	protected T poll() {
		for (;;) {
			final SoftReference<T> ref = queue.poll();
			if (ref == null) {
				return null;
			}
			final T value = ref.get();
			if (value != null) {
				return value;
			}
		}
	}

	@Override
	protected boolean offer(T value) {
		return queue.offer(new SoftReference<T>(value));
	}

	@Override
	public int getNumPooled() {
		return queue.size();
	}
}
