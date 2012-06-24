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
package ch.bind.philib.io;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import ch.bind.philib.validation.Validation;

/**
 * An evented queue.
 */
public final class EventedQueue<E> {

	private final Semaphore sem;

	private final Queue<E> queue;

	public EventedQueue() {
		this(new ConcurrentLinkedQueue<E>());
	}

	public EventedQueue(Queue<E> queue) {
		Validation.notNull(queue);
		this.sem = new Semaphore(0);
		this.queue = queue;
	}

	public boolean offer(E e) {
		boolean added = queue.add(e);
		if (added) {
			sem.release();
		}
		return added;
	}

	public int size() {
		return queue.size();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	// public void clear() {
	// sem.drainPermits();
	// queue.clear();
	// }

	public E poll() {
		if (sem.tryAcquire()) {
			return queue.poll();
		} else {
			return null;
		}
	}

	public E peek() {
		return queue.peek();
	}

}
