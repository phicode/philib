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

import java.util.concurrent.atomic.AtomicInteger;

import ch.bind.philib.validation.Validation;

public class RingQueue<E> {

	private AtomicInteger writeIdx = new AtomicInteger();
	private AtomicInteger writeCommittedIdx = new AtomicInteger(-1);
	private AtomicInteger readIdx = new AtomicInteger(-1);
	private E[] entries;

	public RingQueue(int numEntries) {
		Validation.isTrue(numEntries > 0, "numEntries must be > 0");
		this.entries = (E[]) new Object[numEntries];
	}

	public boolean offer(E e) {
		Validation.notNull(e);
		int wIdx = writeIdx.get();
		return false;
	}
}
