package ch.bind.philib.cache.impl;

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.cache.ObjectCache;
import ch.bind.philib.validation.SimpleValidation;

public final class ScalableObjectCache<E> implements ObjectCache<E> {

	private CacheThreadLocal cacheByThread = new CacheThreadLocal();

	private LinkedObjectCache<E>[] caches;

	private AtomicLong usageCount = new AtomicLong(0);

	public ScalableObjectCache(ObjectFactory<E> factory, int maxEntries) {
		this(factory, maxEntries, Runtime.getRuntime().availableProcessors());
	}

	public ScalableObjectCache(ObjectFactory<E> factory, int maxEntries, int numBuckets) {
		SimpleValidation.notNull(factory);
		int entriesPerBucket = maxEntries / numBuckets;
		if (maxEntries % numBuckets != 0) {
			entriesPerBucket++;
		}
		caches = new LinkedObjectCache[numBuckets];
		for (int i = 0; i < numBuckets; i++) {
			caches[i] = new LinkedObjectCache<E>(factory, entriesPerBucket);
		}
	}

	@Override
	public E acquire() {
		return cacheByThread.get().acquire();
	}

	@Override
	public boolean release(E e) {
		return cacheByThread.get().release(e);
	}

	private LinkedObjectCache<E> bind() {
		// round robin distribution in the order the threads first access the
		// cache
		long v = usageCount.getAndIncrement();
		int cacheIdx = (int) (v % caches.length);
		return caches[cacheIdx];
	}

	private final class CacheThreadLocal extends ThreadLocal<LinkedObjectCache<E>> {

		@Override
		protected LinkedObjectCache<E> initialValue() {
			return bind();
		}
	}
}
