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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.manager.ObjectManager;

public class ObjectPoolTest {

	@Test
	public void halfRecycleable() {
		Pool<Integer> pool = new StrongRefPool<Integer>(new RecycleOddManager(), 8);
		for (int i = 0; i < 10; i++) {
			assertEquals(pool.take().intValue(), i);
		}
		assertEquals(pool.getNumPooled(), 0);
		for (int i = 0; i < 10; i++) {
			pool.recycle(Integer.valueOf(i));
		}
		// 1, 3, 5, 7, 9 => 5
		assertEquals(pool.getNumPooled(), 5);
		for (int i = 1; i < 10; i += 2) {
			assertEquals(pool.take().intValue(), i);
		}
		assertEquals(pool.take().intValue(), 10);
		assertEquals(pool.getNumPooled(), 0);
		assertEquals(pool.getPoolStats().toString(), "creates=11, takes=16, recycled=5, released=5");
	}

	private static final class RecycleOddManager implements ObjectManager<Integer> {

		private int next;

		@Override
		public Integer create() {
			return Integer.valueOf(next++);
		}

		@Override
		public void release(Integer value) {
		}

		@Override
		public boolean prepareForRecycle(Integer value) {
			return (value.intValue() & 1) == 1;
		}

		@Override
		public boolean canReuse(Integer value) {
			return true;
		}
	}
}
