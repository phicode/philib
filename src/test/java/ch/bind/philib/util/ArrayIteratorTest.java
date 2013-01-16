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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.testng.annotations.Test;

public class ArrayIteratorTest {

	@Test
	public void regular() {
		Integer[] xs = {
				1, 2, 3, };
		ArrayIterator<Integer> iter = new ArrayIterator<Integer>(xs);

		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 1);
		verifyNoRemove(iter);

		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 2);
		verifyNoRemove(iter);

		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 3);
		verifyNoRemove(iter);

		verifyEnd(iter);
	}

	@Test
	public void empty() {
		Integer[] xs = {};
		ArrayIterator<Integer> iter = new ArrayIterator<Integer>(xs);
		verifyEnd(iter);
	}

	@Test
	public void nullArray() {
		Integer[] xs = null;
		ArrayIterator<Integer> iter = new ArrayIterator<Integer>(xs);
		verifyEnd(iter);
	}

	private static void verifyEnd(Iterator<Integer> iter) {
		assertFalse(iter.hasNext());
		try {
			iter.next();
			fail();
		} catch (Exception e) {
			assertEquals(e.getClass(), NoSuchElementException.class);
		}
	}

	private static void verifyNoRemove(Iterator<Integer> iter) {
		try {
			iter.remove();
			fail();
		} catch (Exception e) {
			assertEquals(e.getClass(), UnsupportedOperationException.class);
		}
	}
}
