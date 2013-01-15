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

import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.PoolStats;
import ch.bind.philib.pool.manager.ObjectManager;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public abstract class PoolBase<T> implements Pool<T> {

	private final ObjectManager<T> manager;

	private final SimplePoolStats stats = new SimplePoolStats();

	public PoolBase(ObjectManager<T> manager) {
		Validation.notNull(manager);
		this.manager = manager;
	}

	@Override
	public final T take() {
		stats.incrementTakes();
		do {
			T e = poll();
			if (e == null) {
				stats.incrementCreates();
				return manager.create();
			}
			if (manager.canReuse(e)) {
				return e;
			}
			stats.incrementReleased();
			manager.release(e);
		} while (true);
	}

	@Override
	public final void recycle(final T value) {
		if (value != null) {
			if (manager.prepareForRecycle(value)) {
				if (offer(value)) {
					stats.incrementRecycled();
					return;
				}
			}
			stats.incrementReleased();
			manager.release(value);
		}
	}

	@Override
	public final void clear() {
		T e = null;
		while ((e = poll()) != null) {
			stats.incrementReleased();
			manager.release(e);
		}
	}

	@Override
	public final PoolStats getPoolStats() {
		return stats;
	}

	protected abstract T poll();

	protected abstract boolean offer(T value);
}
