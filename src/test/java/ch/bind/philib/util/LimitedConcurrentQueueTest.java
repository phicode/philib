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

package ch.bind.philib.util;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class LimitedConcurrentQueueTest {

	@Test
	public void offer() {
		LimitedConcurrentQueue<Integer> lcq = new LimitedConcurrentQueue<Integer>(2);
		assertEquals(lcq.getLimit(), 2);
		assertTrue(lcq.offer(1));
		assertTrue(lcq.offer(2));
		assertFalse(lcq.offer(3));
	}

	@Test
	public void poll() {
		LimitedConcurrentQueue<Integer> lcq = new LimitedConcurrentQueue<Integer>(2);
		assertNull(lcq.poll());
		assertEquals(lcq.size(), 0);
		assertTrue(lcq.offer(1));
		assertTrue(lcq.offer(2));
		assertFalse(lcq.offer(3));
		assertEquals(lcq.poll().intValue(), 1);
		assertEquals(lcq.poll().intValue(), 2);
		assertNull(lcq.poll());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void noNegativeCtor() {
		new LimitedConcurrentQueue<Integer>(-1);
	}
}
