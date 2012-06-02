package ch.bind.philib.cache.impl;

import ch.bind.philib.cache.CacheStats;
import ch.bind.philib.cache.ObjectCache;
import ch.bind.philib.validation.SimpleValidation;

public abstract class ObjectCacheBase<E> implements ObjectCache<E> {

	private final ObjectFactory<E> factory;

	private final SimpleCacheStats stats = new SimpleCacheStats();

	public ObjectCacheBase(ObjectFactory<E> factory) {
		SimpleValidation.notNull(factory);
		this.factory = factory;
	}

	@Override
	public final E acquire() {
		stats.incrementAcquires();
		do {
			E e = tryAcquire();
			if (e == null) {
				stats.incrementCreates();
				return factory.create();
			} else {
				if (factory.canReuse(e)) {
					return e;
				} else {
					stats.incrementDestroyed();
					factory.destroy(e);
				}
			}
		} while (true);
	}

	@Override
	public final boolean release(final E e) {
		if (e != null && factory.release(e)) {
			if (tryRelease(e)) {
				stats.incrementReleases();
				return true;
			} else {
				stats.incrementDestroyed();
				factory.destroy(e);
			}
		}
		return false;
	}

	@Override
	public final CacheStats getCacheStats() {
		return stats;
	}

	protected abstract E tryAcquire();

	protected abstract boolean tryRelease(E e);
}
