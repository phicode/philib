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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CowSetTest {

	@Test
	public void isEmpty() {
		CowSet<Integer> cs = new CowSet<Integer>(Integer.class);
		assertTrue(cs.isEmpty());
		assertTrue(cs.add(1));
		assertFalse(cs.isEmpty());
		assertTrue(cs.remove(new Integer(1))); // other integer object
		assertTrue(cs.isEmpty());
	}

	@Test
	public void emptyView() {
		CowSet<Integer> cs = new CowSet<Integer>(Integer.class);
		Integer[] empty1 = cs.getView();
		cs.add(1);
		assertEquals(cs.getView().length, 1);
		cs.remove(1);
		Integer[] empty2 = cs.getView();
		assertTrue(empty1 == empty2);
	}

	@Test
	public void noNullAdd() {
		CowSet<Integer> cs = new CowSet<Integer>(Integer.class);
		assertFalse(cs.add(null));
	}

	@Test
	public void noNullRemove() {
		CowSet<Integer> cs = new CowSet<Integer>(Integer.class);
		assertFalse(cs.remove(null));
	}

	@Test
	public void noViewUpdateOnFalseRemove() {
		CowSet<Integer> cs = new CowSet<Integer>(Integer.class);
		cs.add(55);
		Integer[] v = cs.getView();
		assertEquals(v.length, 1);
		assertEquals(v[0], Integer.valueOf(55));
		assertFalse(cs.remove(1));
		assertTrue(v == cs.getView());
	}

	@Test
	public void noDuplicates() {
		CowSet<Integer> cs = new CowSet<Integer>(Integer.class);
		Integer a = 55;
		Integer b = new Integer(55); // different reference but equals
		assertTrue(cs.add(a));
		assertFalse(cs.add(a));
		assertFalse(cs.add(b));
		assertEquals(cs.getView().length, 1);
	}

	@Test
	public void size() {
		CowSet<Integer> cs = new CowSet<Integer>(Integer.class);
		assertEquals(cs.size(), 0);
		cs.add(1);
		assertEquals(cs.size(), 1);
	}
}
