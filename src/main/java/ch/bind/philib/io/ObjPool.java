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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import ch.bind.philib.validation.SimpleValidation;

public abstract class ObjPool<E> {

	private static final int CONC = 8;
	
	private final AtomicBitSet readable;

	private final AtomicBitSet writable;

	private final AtomicReferenceArray<E> pool;

	private final AtomicInteger[] useCount;

	private final int maxEntries;

	// private final AtomicInteger size = new AtomicInteger(0);
	//
	// private final AtomicInteger rpos = new AtomicInteger(0);
	//
	// private final AtomicInteger wpos = new AtomicInteger(0);

	public ObjPool(int maxEntries) {
		this.readable = AtomicBitSet.forNumBits(CONC, false);
		this.writable = AtomicBitSet.forNumBits(CONC, true);
		this.maxEntries = maxEntries;
		this.pool = new AtomicReferenceArray<E>(maxEntries);
		useCount = new AtomicInteger[maxEntries];
		for (int i = 0; i < maxEntries; i++) {
			useCount[i] = new AtomicInteger();
		}
	}

	public void printUseCounts() {
		System.out.println("UseCounts: ");
		for (int i = 0; i < maxEntries; i++) {
			System.out.printf("%10d ", useCount[i].get());
			if (i % 10 == 9) {
				System.out.println();
			}
		}
	}

	protected abstract E create();

	protected abstract void destroy(E e);

	public E get() {
		int idx = readable.switchAnyToFalse();
		if (idx == -1) {
			return create();
		}
		else {
			useCount[idx].incrementAndGet();
			
			E e = pool.getAndSet(idx, null);
			// TODO: make to assert
			SimpleValidation.notNull(e);
			boolean ok = writable.compareFalseAndSwitch(idx);
			SimpleValidation.isTrue(ok);
			return e;
		}
	}

	public void release(final E e) {
		if (e != null) {
			int idx = writable.switchAnyToFalse();
			if (idx == -1) {
				destroy(e);
			}
			else {
				useCount[idx].incrementAndGet();
				
				boolean ok = pool.compareAndSet(idx, null, e);
				// TODO: make to assert
				SimpleValidation.isTrue(ok);
				ok = readable.compareFalseAndSwitch(idx);
				SimpleValidation.isTrue(ok);
			}
		}
	}
}
