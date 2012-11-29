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

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicReference;

import ch.bind.philib.pool.manager.ObjectManager;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class LinkedPool<T> extends PoolBase<T> {

	private final AtomicReference<Node<T>> freeList;

	private final AtomicReference<Node<T>> objList;

	private final Node<T> LOCK_DUMMY = new StrongNode<T>();

//	public LinkedPool(ObjectManager<T> manager, int maxEntries) {
//		this(manager, maxEntries, false);
//	}

	public LinkedPool(ObjectManager<T> manager, int maxEntries, boolean allowLeaks) {
		super(manager);
		Validation.isTrue(maxEntries > 0);
		this.freeList = new AtomicReference<Node<T>>();
		this.objList = new AtomicReference<Node<T>>();
		for (int i = 0; i < maxEntries; i++) {
			if (allowLeaks) {
				put(freeList, new SoftNode<T>());
			}
			else {
				put(freeList, new StrongNode<T>());
			}
		}
	}

	@Override
	protected T tryGetOne() {
		final Node<T> node = take(objList);
		if (node == null) {
			return null;
		}
		final T e = node.unsetValue();
		assert (e != null);
		put(freeList, node);
		return e;
	}

	@Override
	protected boolean tryPutOne(final T value) {
		final Node<T> node = take(freeList);
		if (node != null) {
			node.setValue(value);
			put(objList, node);
			return true;
		}
		return false;
	}

	private final void put(final AtomicReference<Node<T>> rootRef, final Node<T> newHead) {
		assert (rootRef != null);
		assert (newHead != null);
		do {
			final Node<T> tail = rootRef.get();
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

	private final Node<T> take(final AtomicReference<Node<T>> rootRef) {
		assert (rootRef != null);
		do {
			final Node<T> head = rootRef.get();
			if (head == null || head == LOCK_DUMMY) { // empty or locked
				return null;
			}
			if (rootRef.compareAndSet(head, LOCK_DUMMY)) {
				final Node<T> tail = head.getTail();
				boolean ok = rootRef.compareAndSet(LOCK_DUMMY, tail);
				assert (ok);
				return head;
			}
		} while (true);
	}

	private interface Node<T> {
		void setTail(Node<T> tail);

		Node<T> getTail();

		void setValue(T value);

		T unsetValue();
	}

	private static final class SoftNode<T> implements Node<T> {

		private volatile Node<T> tail;

		private volatile SoftReference<T> ref;

		public void setTail(Node<T> tail) {
			this.tail = tail;
		}

		public Node<T> getTail() {
			return tail;
		}

		public void setValue(T value) {
			assert (value != null);
			this.ref = new SoftReference<T>(value);
		}

		public T unsetValue() {
			SoftReference<T> ref = this.ref;
			this.ref = null;
			return ref.get();
		}
	}

	private static final class StrongNode<T> implements Node<T> {

		private volatile Node<T> tail;

		private volatile T value;

		public void setTail(Node<T> tail) {
			this.tail = tail;
		}

		public Node<T> getTail() {
			return tail;
		}

		public void setValue(T value) {
			assert (value != null);
			this.value = value;
		}

		public T unsetValue() {
			T value = this.value;
			this.value = null;
			return value;
		}
	}
}
