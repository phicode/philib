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

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class FinishedFutureTest {

	@Test
	public void withNull() {
		FinishedFuture<Integer> ff = new FinishedFuture<Integer>(null);
		assertFalse(ff.cancel(true));
		assertFalse(ff.cancel(false));
		assertNull(ff.get());
		assertNull(ff.get(1, TimeUnit.MICROSECONDS));
		assertFalse(ff.isCancelled());
		assertTrue(ff.isDone());
	}

	@Test
	public void withValue() {
		Integer value = 123456;
		FinishedFuture<Integer> ff = new FinishedFuture<Integer>(value);
		assertFalse(ff.cancel(true));
		assertFalse(ff.cancel(false));
		assertEquals(ff.get(), value);
		assertEquals(ff.get(1, TimeUnit.MICROSECONDS), value);
		assertFalse(ff.isCancelled());
		assertTrue(ff.isDone());
	}
}
