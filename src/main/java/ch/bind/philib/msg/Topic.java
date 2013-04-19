/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.msg;

import java.util.concurrent.Callable;

import ch.bind.philib.validation.Validation;

public final class Topic<E> {

	private final String name;

	private final MultiQueue<E> queue = new MultiQueue<E>();

	public Topic(String name) {
		this.name = name;
	}

	public Subscription subscribe(Callable<E> callback) {
		Validation.notNull(callback);
		MultiQueue.Sub<E> sub = queue.subscribe();
		return new Sub(sub, callback);
	}

	public interface Subscription {
		void cancel();
	}

	private static final class Sub<E> implements Subscription {

		private final MultiQueue.Sub<E> sub;

		private final Callable<E> callback;

		public Sub(MultiQueue.Sub<E> sub, Callable<E> callback) {
			this.sub = sub;
			this.callback = callback;
		}

		@Override
		public void cancel() {
			sub.unsubscribe();
		}
	}
}
