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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class CounterRepoTest {

	@Test
	public void reuseExisting() {
		Counter a = CounterRepo.DEFAULT.forName("a", null);
		Counter b = CounterRepo.DEFAULT.forName("b", null);

		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 2);
		Counter a2 = CounterRepo.DEFAULT.forName("a", null);
		Counter b2 = CounterRepo.DEFAULT.forName("b", null);
		assertTrue(a == a2);
		assertTrue(b == b2);
		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 2);
	}

	@Test
	public void remove() {
		Counter a = CounterRepo.DEFAULT.forName("a", null);
		CounterRepo.DEFAULT.remove("a");
		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 0);
		Counter a2 = CounterRepo.DEFAULT.forName("a", null);
		assertTrue(a != null && a2 != null && a != a2);
	}
}
