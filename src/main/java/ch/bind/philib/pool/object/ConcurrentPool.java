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

package ch.bind.philib.pool.object;

import ch.bind.philib.math.Calc;
import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.PoolStats;
import ch.bind.philib.pool.manager.ObjectManager;
import ch.bind.philib.validation.Validation;

/**
 * @author Philipp Meinen
 */
public final class ConcurrentPool<T> implements Pool<T> {

	private final PoolBase<T>[] pools;

	private final MultiPoolStats stats;

	@SuppressWarnings("unchecked")
	public ConcurrentPool(ObjectManager<T> manager, int maxEntries, boolean softRefs, int concurrencyLevel) {
		Validation.notNull(manager, "no object manager provided");
		Validation.isTrue(maxEntries > 0, "wont create an empty object pool");
		if (concurrencyLevel < 2) {
			concurrencyLevel = 2;
		}

		int maxPerPool = Calc.ceilDiv(maxEntries, concurrencyLevel);
		this.pools = new PoolBase[concurrencyLevel];
		PoolStats[] s = new PoolStats[concurrencyLevel];
		for (int i = 0; i < concurrencyLevel; i++) {
			if (softRefs) {
				pools[i] = new SoftRefPool<>(manager, maxPerPool);
			} else {
				pools[i] = new StrongRefPool<>(manager, maxPerPool);
			}
			s[i] = pools[i].getPoolStats();
		}
		this.stats = new MultiPoolStats(s);
	}

	@Override
	public T take() {
		return getPoolByThread().take();
	}

	@Override
	public void recycle(T value) {
		getPoolByThread().recycle(value);
	}

	@Override
	public PoolStats getPoolStats() {
		return stats;
	}

	@Override
	public int getNumPooled() {
		int total = 0;
		for (PoolBase<T> pool : pools) {
			total += pool.getNumPooled();
		}
		return total;
	}

	@Override
	public void clear() {
		getPoolByThread().clear();
	}

	public int getConcurrency() {
		return pools.length;
	}

	private PoolBase<T> getPoolByThread() {
		int idx = (int) (Thread.currentThread().getId() % pools.length);
		return pools[idx];
	}
}
