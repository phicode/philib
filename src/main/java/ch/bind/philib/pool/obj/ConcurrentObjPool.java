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

package ch.bind.philib.pool.obj;

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.cache.buf.BufCache;
import ch.bind.philib.cache.buf.Stats;
import ch.bind.philib.cache.buf.factory.BufferFactory;
import ch.bind.philib.math.PhiMath;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class ConcurrentObjPool<E> implements BufCache<E> {

	private final CacheThreadLocal cacheByThread = new CacheThreadLocal();

	private final LinkedObjectCache<E>[] caches;

	private final AtomicLong usageCount = new AtomicLong(0);

	private final MultiStats stats;

	@SuppressWarnings("unchecked")
	public ConcurrentObjPool(BufferFactory<E> factory, int maxEntries, int concurrencyLevel) {
		Validation.notNull(factory, "no buffer factory provided");
		Validation.isTrue(maxEntries > 0, "wont create an empty buffer cache");
		if (concurrencyLevel < 2) {
			concurrencyLevel = 2;
		}

		int entriesPerCache = (int) PhiMath.ceilDiv(maxEntries, concurrencyLevel);
		this.caches = new LinkedObjectCache[concurrencyLevel];
		Stats[] s = new Stats[concurrencyLevel];
		for (int i = 0; i < concurrencyLevel; i++) {
			caches[i] = new LinkedObjectCache<E>(factory, entriesPerCache);
			s[i] = caches[i].getCacheStats();
		}
		this.stats = new MultiStats(s);
	}

	@Override
	public E acquire() {
		return cacheByThread.get().acquire();
	}

	@Override
	public void free(E e) {
		cacheByThread.get().free(e);
	}

	@Override
	public Stats getCacheStats() {
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
			return ConcurrentBufferCache.this.bind();
		}
	}
}
