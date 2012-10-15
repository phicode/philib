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

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class LruListTest {

	@Test
	public void autoRemoveTail() {
		Node a = new Node(), b = new Node(), c = new Node();

		LruList lru = new LruList(2);
		assertNull(lru.add(a));
		assertNull(lru.add(b));
		assertEquals(lru.add(c), a);
	}

	@Test
	public void moveToHead() {
		Node a = new Node(), b = new Node(), c = new Node();

		LruList lru = new LruList(2);
		assertNull(lru.add(a));
		assertNull(lru.add(b));
		lru.moveToHead(a);
		assertEquals(lru.add(c), b);
		lru.moveToHead(c);
		assertEquals(lru.add(b), a);
	}

	@Test
	public void clear() {
		Node a = new Node(), b = new Node();

		LruList lru = new LruList(2);
		assertNull(lru.add(a));
		assertNull(lru.add(b));
		assertEquals(lru.size(), 2);
		lru.clear();
		assertEquals(lru.size(), 0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ctorValidation() {
		new LruList(0);
	}

	@Test
	public void fullScenario() {
		Node a = new Node(), b = new Node(), c = new Node();

		LruList lru = new LruList(3);
		assertEquals(lru.capacity(), 3);
		lru.add(a);
		assertEquals(lru.size(), 1);
		lru.add(b);
		assertEquals(lru.size(), 2);
		lru.add(c);
		assertEquals(lru.size(), 3);
		lru.moveToHead(a);
		lru.moveToHead(b);
		lru.moveToHead(c);
		lru.moveToHead(b);
		lru.moveToHead(a);
		assertEquals(lru.size(), 3);
		// remove from the middle
		lru.remove(b);
		assertEquals(lru.size(), 2);
		lru.add(b);
		assertEquals(lru.size(), 3);
		lru.moveToHead(a);
		// remove from the tail
		lru.remove(c);
		assertEquals(lru.size(), 2);
		lru.add(c);
		assertEquals(lru.size(), 3);
		lru.moveToHead(b);
		lru.moveToHead(a);
		// remove from head
		lru.remove(a);
		assertEquals(lru.size(), 2);
		lru.remove(b);
		assertEquals(lru.size(), 1);
		lru.remove(c);
		assertEquals(lru.size(), 0);
	}

	private static final class Node implements LruNode {

		private LruNode next;

		private LruNode prev;

		@Override
		public void setNext(LruNode next) {
			this.next = next;
		}

		@Override
		public void setPrev(LruNode prev) {
			this.prev = prev;
		}

		@Override
		public LruNode getNext() {
			return next;
		}

		@Override
		public LruNode getPrev() {
			return prev;
		}

		@Override
		public void reset() {
			next = null;
			prev = null;
		}
	}
}
