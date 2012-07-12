/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.bind.philib.cache.impl;

import ch.bind.philib.cache.CacheStats;
import ch.bind.philib.cache.ObjectCache;
import ch.bind.philib.validation.Validation;

public abstract class ObjectCacheBase<E> implements ObjectCache<E> {

	private final ObjectFactory<E> factory;

	private final SimpleCacheStats stats = new SimpleCacheStats();

	public ObjectCacheBase(ObjectFactory<E> factory) {
		Validation.notNull(factory);
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
			}
			if (factory.canReuse(e)) {
				return e;
			}
			stats.incrementDestroyed();
			factory.destroy(e);
		} while (true);
	}

	@Override
	public final boolean release(final E e) {
		if (e != null && factory.release(e)) {
			if (tryRelease(e)) {
				stats.incrementReleases();
				return true;
			}
			stats.incrementDestroyed();
			factory.destroy(e);
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
