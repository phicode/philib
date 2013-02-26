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

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import ch.bind.philib.validation.Validation;

/**
 * @author Philipp Meinen
 */
public class AQ<E> extends AbstractQueue<E> implements Queue<E> {

	// private static final AtomicReferenceFieldUpdater<Node, Node> updateNodeNext =
	// AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");

	private static final class Node<E> {

		final E e;

		volatile Node<E> next;

		public Node(E e) {
			this.e = e;
		}

	}

	private final AtomicReference<Node<E>> head = new AtomicReference<Node<E>>();

	private final AtomicReference<Node<E>> tail = new AtomicReference<Node<E>>();

	@Override
	public void clear() {
		tail.set(null);
		head.set(null);
	}

	@Override
	public E poll() {
		while (true) {
			Node<E> h = head.get();
			if (h == null) {
				return null;
			}
			Node<E> n = h.next;
			if (head.compareAndSet(h, n)) {
				return h.e;
			}
		}
	}

	@Override
	public boolean offer(final E e) {
		// TODO: set head
		Validation.notNull(e);
		final Node<E> n = new Node<E>(e);
		while (true) {
			Node<E> t = tail.get();
			if (t != null) {
				Node<E> tt = t.next;
				while (tt != null) {
					t = tt;
				}
			}
			if (tail.compareAndSet(t, n)) {
				if (t != null) {
					t.next = n;
				}
				return true;
			}
			// if (updateNodeNext.compareAndSet(t, null, n)) {
			// tail.set(n);
			// } else {
			// Thread.yield();
			// }
		}
	}

	@Override
	public E peek() {
		Node<E> h = head.get();
		return h == null ? null : h.e;
	}

	@Override
	public boolean contains(Object o) {
		Node<E> h = head.get();
		while (h != null) {
			if (h.e.equals(o)) {
				return true;
			}
			h = h.next;
		}
		return false;
	}

	@Override
	public int size() {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean isEmpty() {
		return head.get() == null;
	}

	@Override
	public Iterator<E> iterator() {
		return new Iter<E>(head.get());
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	private static final class Iter<E> implements Iterator<E> {

		private Node<E> node;

		Iter(Node<E> node) {
			this.node = node;
		}

		@Override
		public boolean hasNext() {
			return node != null;
		}

		@Override
		public E next() {
			if (node == null) {
				throw new NoSuchElementException();
			}
			E rv = node.e;
			node = node.next;
			return rv;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
