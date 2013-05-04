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

import ch.bind.philib.util.SimpleCowList;
import static org.testng.Assert.*;

public class SimpleCowListTest {

	@Test
	public void isEmpty() {
		SimpleCowList<Integer> scl = new SimpleCowList<Integer>(Integer.class);
		assertTrue(scl.isEmpty());
		assertTrue(scl.add(1));
		assertFalse(scl.isEmpty());
		assertTrue(scl.remove(new Integer(1))); // other integer object
		assertTrue(scl.isEmpty());
	}

	@Test
	public void emptyView() {
		SimpleCowList<Integer> scl = new SimpleCowList<Integer>(Integer.class);
		Integer[] empty1 = scl.getView();
		scl.add(1);
		assertEquals(scl.getView().length, 1);
		scl.remove(1);
		Integer[] empty2 = scl.getView();
		assertTrue(empty1 == empty2);
	}

	@Test
	public void noNullAdd() {
		SimpleCowList<Integer> scl = new SimpleCowList<Integer>(Integer.class);
		assertFalse(scl.add(null));
	}

	@Test
	public void noNullRemove() {
		SimpleCowList<Integer> scl = new SimpleCowList<Integer>(Integer.class);
		assertFalse(scl.remove(null));
	}

	@Test
	public void noViewUpdateOnFalseRemove() {
		SimpleCowList<Integer> scl = new SimpleCowList<Integer>(Integer.class);
		scl.add(55);
		Integer[] v = scl.getView();
		assertEquals(v.length, 1);
		assertEquals(v[0], Integer.valueOf(55));
		assertFalse(scl.remove(1));
		assertTrue(v == scl.getView());
	}
}
