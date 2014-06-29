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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CounterRepoTest {

	@AfterMethod
	public void afterMethod() {
		CounterRepo.DEFAULT.clear();
	}

	@Test
	public void reuseExisting() {
		Counter a1 = CounterRepo.DEFAULT.forName("a", null);
		Counter a2 = CounterRepo.DEFAULT.forName("a");
		Counter b1 = CounterRepo.DEFAULT.forName("b", null);
		Counter b2 = CounterRepo.DEFAULT.forName("b");
		Counter i1 = CounterRepo.DEFAULT.forClass(Integer.class, null);
		Counter i2 = CounterRepo.DEFAULT.forClass(Integer.class);
		Counter s1 = CounterRepo.DEFAULT.forClass(String.class, null);
		Counter s2 = CounterRepo.DEFAULT.forClass(String.class);
		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 4);

		Counter ap1 = CounterRepo.DEFAULT.forName("a", "x");
		Counter ap2 = CounterRepo.DEFAULT.forName("a", "x");
		Counter bp1 = CounterRepo.DEFAULT.forName("b", "x");
		Counter bp2 = CounterRepo.DEFAULT.forName("b", "x");
		Counter ip1 = CounterRepo.DEFAULT.forClass(Integer.class, "x");
		Counter ip2 = CounterRepo.DEFAULT.forClass(Integer.class, "x");
		Counter sp1 = CounterRepo.DEFAULT.forClass(String.class, "x");
		Counter sp2 = CounterRepo.DEFAULT.forClass(String.class, "x");
		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 8);

		assertTrue(a1 == a2);
		assertTrue(b1 == b2);
		assertTrue(i1 == i2);
		assertTrue(s1 == s2);

		assertTrue(ap1 == ap2);
		assertTrue(bp1 == bp2);
		assertTrue(ip1 == ip2);
		assertTrue(sp1 == sp2);
	}

	@Test
	public void remove() {
		Counter a1 = CounterRepo.DEFAULT.forName("a");
		Counter a2 = CounterRepo.DEFAULT.forName("a", "x");
		Counter i1 = CounterRepo.DEFAULT.forClass(Integer.class);
		Counter i2 = CounterRepo.DEFAULT.forClass(Integer.class, "x");
		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 4);

		CounterRepo.DEFAULT.remove("a");
		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 3);
		CounterRepo.DEFAULT.remove("a", "x");
		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 2);
		CounterRepo.DEFAULT.remove(Integer.class);
		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 1);
		CounterRepo.DEFAULT.remove(Integer.class, "x");
		assertEquals(CounterRepo.DEFAULT.getCounters().size(), 0);

		Counter otherA1 = CounterRepo.DEFAULT.forName("a");
		Counter otherA2 = CounterRepo.DEFAULT.forName("a", "x");
		Counter otherI1 = CounterRepo.DEFAULT.forClass(Integer.class);
		Counter otherI2 = CounterRepo.DEFAULT.forClass(Integer.class, "x");
		assertTrue(a1 != otherA1 && a2 != otherA2 && i1 != otherI1 && i2 != otherI2);
	}

	@Test
	public void noName() {
		Counter n1 = CounterRepo.DEFAULT.forName(null);
		Counter n2 = CounterRepo.DEFAULT.forName(null, "x");
		Counter n3 = CounterRepo.DEFAULT.forName("");
		Counter n4 = CounterRepo.DEFAULT.forName("", "x");
		assertEquals(n1.getName(), "default");
		assertEquals(n2.getName(), "default:x");
		assertEquals(n3.getName(), "default");
		assertEquals(n4.getName(), "default:x");

		Counter c1 = CounterRepo.DEFAULT.forClass(null);
		Counter c2 = CounterRepo.DEFAULT.forClass(null, "x");
		assertEquals(c1.getName(), "default");
		assertEquals(c2.getName(), "default:x");

		assertTrue(n1 == n3 && n1 == c1);
		assertTrue(n2 == n4 && n2 == c2);
	}
}
