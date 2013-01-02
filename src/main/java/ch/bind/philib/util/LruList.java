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

import ch.bind.philib.validation.Validation;

public final class LruList<E extends LruNode> {

	private final int capacity;

	private final HeadTailNode headTail = new HeadTailNode();

	private int size;

	public LruList(int capacity) {
		Validation.isTrue(capacity > 0, "capacity must be > 0");
		this.capacity = capacity;
		link(headTail, headTail);
	}

	/**
	 * Add a new {@code LruNode} to the head of the LRU.
	 * 
	 * @param node The new head of the {@code LruList}.
	 * @return {@code null} if the the size after adding the new {@code LruNode}
	 *         does not exceed this list's {@code capacity}. Otherwise the list
	 *         will remove the tail (the element which wasn't accessed for the
	 *         longest amount of time) and return it.
	 */
	public E add(final E node) {
		assert (node.getLruPrev() == null && node.getLruNext() == null);

		LruNode afterHead = headTail.getLruNext();
		link(headTail, node);
		link(node, afterHead);

		size++;
		if (size <= capacity) {
			return null;
		}
		return removeTail();
	}

	public void remove(final E node) {
		final LruNode prev = node.getLruPrev();
		final LruNode next = node.getLruNext();

		assert (next != null && prev != null);

		link(prev, next);

		size--;
		node.resetLruNode();
	}

	public E removeTail() {
		if (size == 0) {
			return null;
		}
		final E node = (E) headTail.getLruPrev();
		remove(node);
		return node;
	}

	public void moveToHead(final E node) {
		assert (size > 0);

		final LruNode prev = node.getLruPrev();
		final LruNode next = node.getLruNext();

		if (prev == headTail) {
			// LRU with size 1 or the the node is already in head position
			return;
		}

		// remove
		link(prev, next);

		// add
		LruNode afterHead = headTail.getLruNext();
		link(headTail, node);
		link(node, afterHead);
	}

	public void clear() {
		if (size > 0) {
			LruNode node = headTail.getLruNext();
			while (node != null) {
				LruNode next = node.getLruNext();
				node.resetLruNode();
				node = next;
			}
		}
		size = 0;
		link(headTail, headTail);
	}

	public int size() {
		return size;
	}

	public int capacity() {
		return capacity;
	}

	public boolean hasSpace() {
		return size < capacity;
	}
	
	private void link(LruNode first, LruNode second) {
		first.setLruNext(second);
		second.setLruPrev(first);
	}

	static final class HeadTailNode implements LruNode {

		private LruNode next;

		private LruNode prev;

		@Override
		public void setLruNext(LruNode lruNext) {
			this.next = lruNext;
		}

		@Override
		public void setLruPrev(LruNode lruPrev) {
			this.prev = lruPrev;
		}

		@Override
		public LruNode getLruNext() {
			return next;
		}

		@Override
		public LruNode getLruPrev() {
			return prev;
		}

		@Override
		public void resetLruNode() {
			next = null;
			prev = null;
		}
	}
}
