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

package ch.bind.philib.cache.lru.newimpl;

import ch.bind.philib.validation.Validation;

public final class LruList {

	private final int capacity;

	private LruNode head;

	private LruNode tail;

	private int size;

	public LruList(int capacity) {
		Validation.isTrue(capacity > 0, "capacity must be > 0");
		this.capacity = capacity;
	}

	/**
	 * Add a new {@code LruNode} to the head of the LRU.
	 * 
	 * @param node
	 *            The new head of the {@code LruList}.
	 * @return {@code null} if the the size after adding the new {@code LruNode}
	 *         does not exceed this list's {@code capacity}. Otherwise the list
	 *         will remove the tail (the element which wasn't accessed for the
	 *         longest amount of time) and return it.
	 */
	public LruNode add(final LruNode node) {
		assert (node.getPrev() == null && node.getNext() == null);

		if (head == null) {
			assert (tail == null);
			// empty LRU
			head = node;
			tail = node;
		} else {
			assert (tail != null);
			// non-empty LRU
			node.setNext(head);
			head.setPrev(node);
			head = node;
		}
		size++;
		if (size <= capacity) {
			return null;
		}
		return removeTail();
	}

	public void remove(final LruNode node) {
		assert (head != null && tail != null);

		final LruNode prev = node.getPrev();
		final LruNode next = node.getNext();

		if (head == node) {
			if (tail == node) {
				assert (next == null && prev == null);
				// this was the only element in the LRU
				head = null;
				tail = null;
			} else {
				assert (prev == null && next.getPrev() == node);
				// node is at the head of the LRU
				next.setPrev(null);
				head = next;
			}
		} else {
			if (tail == node) {
				assert (next == null && prev.getNext() == node);
				// node is at the tail of the LRU
				prev.setNext(null);
				tail = prev;
			} else {
				assert (prev != null && next != null && prev.getNext() == node && next.getPrev() == node);
				// node is is the middle of the LRU
				prev.setNext(next);
				next.setPrev(prev);
			}
		}
		size--;
		node.reset();
	}

	public void moveToHead(final LruNode node) {
		assert (head != null && tail != null);

		if (head == node) {
			// LRU with size 1 or the the node is already in head position
			return;
		}
		final LruNode prev = node.getPrev();
		final LruNode next = node.getNext();

		// since this node is not the head there are 2
		// or more elements in the LRU
		if (tail == node) {
			assert (prev != null && next == null && prev.getNext() == node);
			// move from tail to head
			node.setPrev(null);
			prev.setNext(null);
			head.setPrev(node);
			node.setNext(head);
			head = node;
			tail = prev;
		} else {
			assert (prev != null && next != null && prev.getNext() == node && next.getPrev() == node);
			// node is is the middle of the LRU -> unlink
			prev.setNext(next);
			next.setPrev(prev);
			node.setNext(head);
			node.setPrev(null);
			head.setPrev(node);
			head = node;
		}
	}

	public LruNode removeTail() {
		assert (head != null && tail != null);

		final LruNode node = tail;
		final LruNode prev = node.getPrev();
		prev.setNext(null);
		tail = prev;
		size--;
		node.reset();
		return node;
	}

	// TODO: clean clear() implementation which calls reset() on every node
	public void clear() {
		size = 0;
		head = null;
		tail = null;
	}

	public int size() {
		return size;
	}

	public int capacity() {
		return capacity;
	}
}
