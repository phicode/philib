package ch.bind.philib.pool.object;

import ch.bind.philib.pool.manager.ObjectManager;
import ch.bind.philib.util.LimitedConcurrentQueue;

public final class StrongPool<T> extends PoolBase<T> {

	private final LimitedConcurrentQueue<T> queue;

	public StrongPool(ObjectManager<T> manager, int maxEntries) {
		super(manager);
		queue = new LimitedConcurrentQueue<T>(maxEntries);
	}

	@Override
	protected T poll() {
		return queue.poll();
	}

	@Override
	protected boolean offer(T value) {
		return queue.offer(value);
	}

	@Override
	public int getNumPooled() {
		return queue.size();
	}
}
