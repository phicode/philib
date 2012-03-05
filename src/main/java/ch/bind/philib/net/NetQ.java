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
package ch.bind.philib.net;

import java.util.concurrent.Semaphore;

import ch.bind.philib.io.NQueue;
import ch.bind.philib.validation.SimpleValidation;

public final class NetQ<E> {

	private final NQueue<E> out;

	private final NQueue<E> in;

	private NetQ(Semaphore inSem, Semaphore outSem) {
		out = new NQueue<E>(inSem);
		in = new NQueue<E>(outSem);
	}

	public static <E> NetQ<E> create(Semaphore inSem, Semaphore outSem) {
		SimpleValidation.notNull(inSem);
		SimpleValidation.notNull(outSem);
		return new NetQ<E>(inSem, outSem);
	}

	public NQueue<E> getOut() {
		return out;
	}

	public NQueue<E> getIn() {
		return in;
	}
}
