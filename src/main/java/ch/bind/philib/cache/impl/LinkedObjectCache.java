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
package ch.bind.philib.cache.impl;

import java.util.concurrent.atomic.AtomicReference;

import ch.bind.philib.validation.Validation;

public final class LinkedObjectCache<E> extends ObjectCacheBase<E> {

	private final AtomicReference<Node<E>> freeList;

	private final AtomicReference<Node<E>> objList;

	private final Node<E> LOCK_DUMMY = new Node<E>();

	public LinkedObjectCache(ObjectFactory<E> factory, int maxEntries) {
		super(factory);
		Validation.isTrue(maxEntries > 0);
		this.freeList = new AtomicReference<Node<E>>();
		this.objList = new AtomicReference<Node<E>>();
		for (int i = 0; i < maxEntries; i++) {
			Node<E> n = new Node<E>();
			put(freeList, n);
		}
	}

	@Override
	protected E tryAcquire() {
		final Node<E> node = take(objList);
		if (node == null) {
			return null;
		} else {
			final E e = node.unsetEntry();
			assert (e != null);
			put(freeList, node);
			return e;
		}
	}

	@Override
	protected boolean tryRelease(E e) {
		final Node<E> node = take(freeList);
		if (node != null) {
			node.setEntry(e);
			put(objList, node);
			return true;
		}
		return false;
	}

	private final void put(final AtomicReference<Node<E>> rootRef, final Node<E> newHead) {
		assert (rootRef != null);
		assert (newHead != null);
		do {
			final Node<E> tail = rootRef.get();
			if (tail != LOCK_DUMMY) {
				if (rootRef.compareAndSet(tail, LOCK_DUMMY)) {
					newHead.setTail(tail);
					boolean ok = rootRef.compareAndSet(LOCK_DUMMY, newHead);
					assert (ok);
					return;
				}
			}
		} while (true);
	}

	private final Node<E> take(final AtomicReference<Node<E>> rootRef) {
		assert (rootRef != null);
		do {
			final Node<E> head = rootRef.get();
			if (head == null || head == LOCK_DUMMY) { // empty or locked
				return null;
			} else {
				if (rootRef.compareAndSet(head, LOCK_DUMMY)) {
					final Node<E> tail = head.getTail();
					boolean ok = rootRef.compareAndSet(LOCK_DUMMY, tail);
					assert (ok);
					head.unsetTail();
					return head;
				}
			}
		} while (true);
	}

	private static final class Node<E> {

		private volatile Node<E> tail;

		private volatile E entry;

		void setTail(Node<E> t) {
			this.tail = t;
		}

		Node<E> getTail() {
			return tail;
		}

		void unsetTail() {
			tail = null;
		}

		void setEntry(E e) {
			assert (e != null);
			this.entry = e;
		}

		E unsetEntry() {
			E e = this.entry;
			this.entry = null;
			return e;
		}
	}
}
