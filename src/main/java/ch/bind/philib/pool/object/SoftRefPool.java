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

import ch.bind.philib.pool.manager.ObjectManager;
import ch.bind.philib.util.LimitedConcurrentQueue;

import java.lang.ref.SoftReference;

public final class SoftRefPool<T> extends PoolBase<T> {

	private final LimitedConcurrentQueue<SoftReference<T>> queue;

	public SoftRefPool(ObjectManager<T> manager, int maxEntries) {
		super(manager);
		queue = new LimitedConcurrentQueue<SoftReference<T>>(maxEntries);
	}

	@Override
	protected T poll() {
		while (true) {
			final SoftReference<T> ref = queue.poll();
			if (ref == null) {
				return null;
			}
			final T value = ref.get();
			if (value != null) {
				return value;
			}
		}
	}

	@Override
	protected boolean offer(T value) {
		return queue.offer(new SoftReference<T>(value));
	}

	@Override
	public int getNumPooled() {
		return queue.size();
	}
}
