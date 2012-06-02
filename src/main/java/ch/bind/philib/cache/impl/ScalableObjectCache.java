package ch.bind.philib.cache.impl;

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.cache.CacheStats;
import ch.bind.philib.cache.ObjectCache;
import ch.bind.philib.validation.SimpleValidation;

public final class ScalableObjectCache<E> implements ObjectCache<E> {

	private final CacheThreadLocal cacheByThread = new CacheThreadLocal();

	private final LinkedObjectCache<E>[] caches;

	private final AtomicLong usageCount = new AtomicLong(0);

	private final CombinedCacheStats stats;

	public ScalableObjectCache(ObjectFactory<E> factory, int maxEntries) {
		this(factory, maxEntries, Runtime.getRuntime().availableProcessors());
	}

	@SuppressWarnings("unchecked")
	public ScalableObjectCache(ObjectFactory<E> factory, int maxEntries, int numBuckets) {
		SimpleValidation.notNull(factory);
		int entriesPerBucket = maxEntries / numBuckets;
		if (maxEntries % numBuckets != 0) {
			entriesPerBucket++;
		}
		this.caches = new LinkedObjectCache[numBuckets];
		CacheStats[] stats = new CacheStats[numBuckets];
		for (int i = 0; i < numBuckets; i++) {
			caches[i] = new LinkedObjectCache<E>(factory, entriesPerBucket);
			stats[i] = caches[i].getCacheStats();
		}
		this.stats = new CombinedCacheStats(stats);
	}

	@Override
	public E acquire() {
		return cacheByThread.get().acquire();
	}

	@Override
	public boolean release(E e) {
		return cacheByThread.get().release(e);
	}

	@Override
	public CacheStats getCacheStats() {
		return stats;
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
