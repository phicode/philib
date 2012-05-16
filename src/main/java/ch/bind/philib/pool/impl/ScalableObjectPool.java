package ch.bind.philib.pool.impl;

import java.util.concurrent.atomic.AtomicLong;


public class ScalableObjectPool<E> implements ObjectPool<E> {

	private PoolThreadLocal poolByThread = new PoolThreadLocal();

	private ObjectPoolImpl<E>[] pools;

	private AtomicLong usageCount = new AtomicLong(0);

	public ScalableObjectPool(int maxEntries) {
		this(maxEntries, Runtime.getRuntime().availableProcessors());
	}

	public ScalableObjectPool(int maxEntries, int numBuckets) {
		int entriesPerBucket = maxEntries / numBuckets;
		if (maxEntries % numBuckets != 0) {
			entriesPerBucket++;
		}
		pools = new ObjectPoolImpl[numBuckets];
		for (int i = 0; i < numBuckets; i++) {
			pools[i] = new ObjectPoolImpl<E>(entriesPerBucket);
		}
	}

	@Override
	public E get(ObjectFactory<E> factory) {
		return poolByThread.get().get(factory);
	}

	@Override
	public void release(ObjectFactory<E> factory, E e) {
		poolByThread.get().release(factory, e);
	}

	private ObjectPoolImpl<E> bind() {
		// round robin distribution in the order the threads first access the
		// pool
		long v = usageCount.getAndIncrement();
		int poolIdx = (int) (v % pools.length);
		return pools[poolIdx];
	}

	private final class PoolThreadLocal extends ThreadLocal<ObjectPoolImpl<E>> {

		@Override
		protected ObjectPoolImpl<E> initialValue() {
			return bind();
		}
	}
}
