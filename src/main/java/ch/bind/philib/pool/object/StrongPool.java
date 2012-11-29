package ch.bind.philib.pool.object;

import ch.bind.philib.pool.manager.ObjectManager;

public final class StrongPool<T> extends PoolBase<T> {

	private final LimitedQueue<T> queue;

	public StrongPool(ObjectManager<T> manager, int maxEntries) {
		super(manager);
		queue = new LimitedQueue<T>(maxEntries);
	}

	@Override
	protected T poll() {
		return queue.poll();
	}

	@Override
	protected boolean offer(T value) {
		return queue.offer(value);
	}
}
