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

import ch.bind.philib.validation.SimpleValidation;

/**
 * A notifying queue. Every time an item is added to the queue semaphore is
 * released.
 */
public final class NQueue<E> {

	private final Semaphore sem;

	private final Queue<E> queue;

	public NQueue(Semaphore sem) {
		this(sem, new ConcurrentLinkedQueue<E>());
	}

	public NQueue(Semaphore sem, Queue<E> queue) {
		SimpleValidation.notNull(sem);
		SimpleValidation.notNull(queue);
		this.sem = sem;
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

	public void clear() {
		queue.clear();
	}

	public E poll() {
		return queue.poll();
	}

	public E peek() {
		return queue.peek();
	}
}
