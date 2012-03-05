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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import ch.bind.philib.validation.SimpleValidation;

public abstract class ObjectPool<E> {

	private static final int NUMLISTS = 1;

	private static final int NUMLISTSMASK = 0;

	// private final AtomicBoolean[] listLocks;

	private final AtomicReference<Node<E>>[] freeLists;

	private final AtomicReference<Node<E>>[] objLists;

	private final Node<E> LOCK_DUMMY = new Node<E>();

	public ObjectPool(int maxEntries) {
		super();
		// this.listLocks = new AtomicBoolean[NUMLISTS];
		this.freeLists = new AtomicReference[NUMLISTS];
		this.objLists = new AtomicReference[NUMLISTS];
		for (int i = 0; i < NUMLISTS; i++) {
			// this.listLocks[i] = new AtomicBoolean(false);
			this.freeLists[i] = new AtomicReference<Node<E>>();
			this.objLists[i] = new AtomicReference<Node<E>>();
		}
		// freeList.set(END_DUMMY);
		// objList.set(END_DUMMY);
		for (int i = 0; i < maxEntries; i++) {
			Node<E> n = new Node<E>();
			int listIdx = i & NUMLISTSMASK;
			n.setInFreeList();
			put(freeLists[listIdx], n);
		}
	}

	protected abstract E create();

	protected abstract void destroy(E e);

	public E get() {
		int firstList = fl();
		for (int i = 0; i < NUMLISTS; i++) {
			int listIdx = (i + firstList) & NUMLISTSMASK;
			E e = tryGet(listIdx);
			if (e != null) {
				return e;
			}
		}
		return create();
	}

	private E tryGet(final int listIdx) {
		AtomicReference<Node<E>> freeList = freeLists[listIdx];
		AtomicReference<Node<E>> objList = objLists[listIdx];
		
		final Node<E> node = take(objList);
		if (node == null) {
			return null;
		} else {
			node.assertInObjList();
			final E e = node.unsetEntry();
			// TODO: remove
			SimpleValidation.notNull(e);
			node.setInFreeList();
			put(freeList, node);
			return e;
		}
	}

	// private AtomicInteger _fl = new AtomicInteger();
	private int _fl;

	private final int fl() {
		return Math.abs(_fl++) & NUMLISTSMASK;
		// return Math.abs(_fl.incrementAndGet()) & NUMLISTSMASK;
		// return (int) (Thread.currentThread().getId() & NUMLISTSMASK);
	}

	public void release(final E e) {
		if (e != null) {
			int firstList = fl();
			for (int i = 0; i < NUMLISTS; i++) {
				int listIdx = (i + firstList) & NUMLISTSMASK;
				if (tryRelease(listIdx, e)) {
					return;
				}
			}
		}
	}

	private boolean tryRelease(int listIdx, E e) {
		AtomicBoolean lock = listLocks[listIdx];
		if (lock.compareAndSet(false, true)) {
			try {
				AtomicReference<Node<E>> freeList = freeLists[listIdx];
				AtomicReference<Node<E>> objList = objLists[listIdx];

				final Node<E> node = take(freeList);
				if (node != null) {
					node.assertInFreeList();
					node.setEntry(e);
					node.setInObjList();
					put(objList, node);
					return true;
				}
			} finally {
				lock.set(false);
			}
		}
		return false;
	}

	private final void put(final AtomicReference<Node<E>> root, final Node<E> head) {
		SimpleValidation.notNull(head);
		do {
			final Node<E> tail = root.get();
			head.setNext(tail);
			if (root.compareAndSet(tail, head)) {
				return;
			}
		} while (true);
	}

	private final Node<E> take(final AtomicReference<Node<E>> root) {
		do {
			final Node<E> head = root.get();
			if (head == null || head == LOCK_DUMMY) { // empty
				return null;
			} else {
				final Node<E> tail = head.getNext();
				if (root.compareAndSet(head, tail)) {
					head.unsetNext();
					return head;
				}
			}
		} while (true);
	}

	// private static final class Node<E> {
	//
	// private volatile Node<E> n;
	//
	// private volatile E e;
	//
	// void setNext(Node<E> n) {
	// //TODO: make this SimpleValidation an assert
	// // SimpleValidation.notNull(n);
	// this.n=n;
	// }
	// public Node<E> getNext() {
	// return n;
	// }
	// // void unsetNext() {
	// // this.n=n;
	// // }
	//
	// void setEntry(E e) {
	// //TODO: make this SimpleValidation an assert
	// SimpleValidation.notNull(e);
	// this.e=e;
	// }
	//
	// E unsetEntry() {
	// E e = this.e;
	// this.e=null;
	// return e;
	// }
	// }

	private static final class Node<E> {
		// private final AtomicBoolean inFreeList = new AtomicBoolean();

		// private final AtomicReference<Node<E>> next = new
		// AtomicReference<Node<E>>();

		// private final AtomicReference<E> entry = new AtomicReference<E>();
		private volatile Node<E> next;

		private volatile E entry;

		void setNext(Node<E> n) {
			// TODO: make this SimpleValidation an assert
			// SimpleValidation.notNull(n);
			// this.next.set(n);
			this.next = n;
		}

		void setInFreeList() {
			// inFreeList.set(true);
		}

		void setInObjList() {
			// inFreeList.set(false);
		}

		void assertInFreeList() {
			// SimpleValidation.isTrue(inFreeList.get());
		}

		void assertInObjList() {
			// SimpleValidation.isFalse(inFreeList.get());
		}

		Node<E> getNext() {
			// return next.get();
			return next;
		}

		void unsetNext() {
			// this.next.set(null);
			next = null;
		}

		void setEntry(E e) {
			// TODO: make this SimpleValidation an assert
			SimpleValidation.notNull(e);
			// this.entry.set(e);
			this.entry = e;
		}

		E unsetEntry() {

			// return entry.getAndSet(null);
			E e = this.entry;
			this.entry = null;
			return e;
		}
	}

}
