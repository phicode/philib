/*
 * Copyright (c) 2006-2011 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.validation;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.HashMap;
import java.util.LinkedList;

import org.testng.annotations.Test;

public class ValidationTest {

	@Test
	public void isFalse() {
		try {
			Validation.isFalse(false);
			Validation.isFalse(false, "foo");
		} catch (IllegalArgumentException e) {
			fail("should not throw");
		}
		try {
			Validation.isFalse(true);
			fail("should throw");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			Validation.isFalse(true, "foo");
			fail("should throw");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void isTrue() {
		try {
			Validation.isTrue(true);
			Validation.isTrue(true, "foo");
		} catch (IllegalArgumentException e) {
			fail("should not throw");
		}
		try {
			Validation.isTrue(false);
			fail("should throw");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			Validation.isTrue(false, "foo");
			fail("should throw");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void notNegative() {
		try {
			Validation.notNegative(0);
			Validation.notNegative(0, "foo");

			Validation.notNegative(0L);
			Validation.notNegative(0L, "foo");

			Validation.notNegative(Integer.MAX_VALUE);
			Validation.notNegative(Integer.MAX_VALUE, "foo");

			Validation.notNegative(Long.MAX_VALUE);
			Validation.notNegative(Long.MAX_VALUE, "foo");
		} catch (IllegalArgumentException e) {
			fail("should not throw");
		}
		try {
			Validation.notNegative(-1);
			fail("should throw");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			Validation.notNegative(-1, "foo");
			fail("should throw");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void notNull() {
		try {
			Validation.notNull("");
			Validation.notNull("", "foo");
		} catch (IllegalArgumentException e) {
			fail("should not throw");
		}
		try {
			Validation.notNull(null);
			fail("should throw");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			Validation.notNull(null, "foo");
			fail("should throw");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void notNullOrEmptyMap() {
		HashMap<String, String> a = null;
		try {
			Validation.notNullOrEmpty(a);
			fail("should throw");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		a = new HashMap<String, String>();
		try {
			Validation.notNullOrEmpty(a);
			fail("should throw");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		a.put("foo", "bar");
		HashMap<String, String> b = Validation.notNullOrEmpty(a);
		assertTrue(a == b);
	}

	@Test
	public void notNullOrEmptyCollection() {
		LinkedList<String> a = null;
		try {
			Validation.notNullOrEmpty(a);
			fail("should throw");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		a = new LinkedList<String>();
		try {
			Validation.notNullOrEmpty(a);
			fail("should throw");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		a.add("foo");
		LinkedList<String> b = Validation.notNullOrEmpty(a);
		assertTrue(a == b);
	}
}
