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

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.cache.CacheStats;
import ch.bind.philib.cache.ObjectCache;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
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
		Validation.notNull(factory);
		int entriesPerBucket = maxEntries / numBuckets;
		if (maxEntries % numBuckets != 0) {
			entriesPerBucket++;
		}
		this.caches = new LinkedObjectCache[numBuckets];
		CacheStats[] s = new CacheStats[numBuckets];
		for (int i = 0; i < numBuckets; i++) {
			caches[i] = new LinkedObjectCache<E>(factory, entriesPerBucket);
			s[i] = caches[i].getCacheStats();
		}
		this.stats = new CombinedCacheStats(s);
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

	LinkedObjectCache<E> bind() {
		// round robin distribution in the order the threads first access the
		// cache
		long v = usageCount.getAndIncrement();
		int cacheIdx = (int) (v % caches.length);
		return caches[cacheIdx];
	}

	private final class CacheThreadLocal extends ThreadLocal<LinkedObjectCache<E>> {

		@Override
		protected LinkedObjectCache<E> initialValue() {
			return ScalableObjectCache.this.bind();
		}
	}
}
