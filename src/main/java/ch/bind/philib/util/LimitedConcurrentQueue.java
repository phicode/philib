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

package ch.bind.philib.util;

import ch.bind.philib.validation.Validation;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class LimitedConcurrentQueue<T> {

	private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<T>();

	private final AtomicInteger size = new AtomicInteger();

	private final int capacity;

	public LimitedConcurrentQueue(int limit) {
		Validation.isTrue(limit > 0);
		this.capacity = limit;
	}

	public T poll() {
		final T value = queue.poll();
		if (value != null) {
			size.decrementAndGet();
		}
		return value;
	}

	public boolean offer(T value) {
		for (; ; ) {
			final int s = size.get();
			if (s >= capacity) {
				return false;
			}
			if (size.compareAndSet(s, s + 1)) {
				return queue.offer(value);
				// NOTE: sun/oracle's ConcurrentLinkedQueue always returns true

				// the interface-contract correct implementation would be:
				// if (queue.offer(value)) {return true;}
				// else { size.decrementAndGet(); return false; }
			}
		}
	}

	public int size() {
		return size.get();
	}

	public int getLimit() {
		return capacity;
	}
}
