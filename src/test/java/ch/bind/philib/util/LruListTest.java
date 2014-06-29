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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

public class LruListTest {

	@Test
	public void removeTailOnOverflow() {
		TestNode a = new TestNode(), b = new TestNode(), c = new TestNode();

		LruList<TestNode> lru = new LruList<TestNode>(2);
		assertNull(lru.add(a));
		assertNull(lru.add(b));
		assertEquals(lru.add(c), a);
	}

	@Test
	public void moveToHead() {
		TestNode a = new TestNode(), b = new TestNode(), c = new TestNode();

		LruList<TestNode> lru = new LruList<TestNode>(2);
		assertNull(lru.add(a));
		assertNull(lru.add(b));
		lru.moveToHead(a);
		assertEquals(lru.add(c), b);
		lru.moveToHead(c);
		assertEquals(lru.add(b), a);
	}

	@Test
	public void clear() {
		TestNode a = new TestNode(), b = new TestNode();

		LruList<TestNode> lru = new LruList<TestNode>(2);
		assertNull(lru.add(a));
		assertNull(lru.add(b));
		assertEquals(lru.size(), 2);
		lru.clear();
		assertEquals(lru.size(), 0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ctorValidation() {
		new LruList<TestNode>(0);
	}

	@Test
	public void removeTail() {
		TestNode a = new TestNode(), b = new TestNode(), c = new TestNode();

		LruList<TestNode> lru = new LruList<TestNode>(3);
		lru.add(a); // lru: a
		lru.add(b); // lru: b, a
		lru.add(c); // lru: c, b, a
		assertEquals(lru.removeTail(), a); // lru: c, b
		assertEquals(lru.size(), 2);
		lru.moveToHead(b);// lru: b, c
		assertEquals(lru.removeTail(), c); // lru: b
		assertEquals(lru.size(), 1);
		assertEquals(lru.removeTail(), b);
		assertEquals(lru.size(), 0);
		assertNull(lru.removeTail());
	}

	@Test
	public void fullScenario() {
		TestNode a = new TestNode(), b = new TestNode(), c = new TestNode();

		LruList<TestNode> lru = new LruList<TestNode>(3);
		assertEquals(lru.capacity(), 3);
		lru.add(a); // lru: a
		assertEquals(lru.size(), 1);
		lru.add(b); // lru: b, a
		assertEquals(lru.size(), 2);
		lru.add(c); // lru: c, b, a
		assertEquals(lru.size(), 3);
		lru.moveToHead(a); // lru: a, c, b
		lru.moveToHead(b); // lru: b, a, c
		lru.moveToHead(c); // lru: c, a, b
		lru.moveToHead(b); // lru: b, a, c
		lru.moveToHead(a); // lru: a, b, c
		assertEquals(lru.size(), 3);
		// remove from the middle
		lru.remove(b); // lru: a, c
		assertEquals(lru.size(), 2);
		lru.add(b); // lru: b, a, c
		assertEquals(lru.size(), 3);
		lru.moveToHead(a); // lru: a, b, c
		// remove from the tail
		lru.remove(c); // lru: a, b
		assertEquals(lru.size(), 2);
		lru.add(c); // lru: c, a, b
		assertEquals(lru.size(), 3);
		lru.moveToHead(b); // lru: b, a, c
		lru.moveToHead(a); // lru: a, b, c
		// remove from head
		lru.remove(a); // lru: b, c
		assertEquals(lru.size(), 2);
		lru.remove(b); // lru: c
		assertEquals(lru.size(), 1);
		lru.remove(c);
		assertEquals(lru.size(), 0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void nonNullAdd() {
		LruList<TestNode> lru = new LruList<TestNode>(1);
		lru.add(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void nonNullRemove() {
		LruList<TestNode> lru = new LruList<TestNode>(1);
		lru.remove(null);
	}

	@Test
	public void addAsserts() {
		LruList<TestNode> lru = new LruList<TestNode>(8);
		TestNode node = new TestNode();
		TestNode bad = new TestNode();

		node.setLruNext(bad);
		try {
			lru.add(node);
			fail();
		} catch (AssertionError e) {
			// expected
		}
		node.setLruNext(null);

		node.setLruPrev(bad);
		try {
			lru.add(node);
			fail();
		} catch (AssertionError e) {
			// expected
		}
		node.setLruPrev(null);
	}

	@Test
	public void removeAsserts() {
		LruList<TestNode> lru = new LruList<TestNode>(8);
		TestNode node = new TestNode();
		TestNode bad = new TestNode();

		node.setLruNext(bad);
		try {
			lru.remove(node);
			fail();
		} catch (AssertionError e) {
			// expected
		}
		node.setLruNext(null);

		node.setLruPrev(bad);
		try {
			lru.remove(node);
			fail();
		} catch (AssertionError e) {
			// expected
		}
		node.setLruPrev(null);
	}

	private static final class TestNode implements LruNode {

		private LruNode next;

		private LruNode prev;

		@Override
		public void setLruNext(LruNode next) {
			this.next = next;
		}

		@Override
		public void setLruPrev(LruNode prev) {
			this.prev = prev;
		}

		@Override
		public LruNode getLruNext() {
			return next;
		}

		@Override
		public LruNode getLruPrev() {
			return prev;
		}
	}
}
