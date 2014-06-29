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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ValidationResultsTest {

	private ValidationResults results;

	@BeforeMethod
	public void beforeMethod() {
		results = new ValidationResults();
	}

	@AfterMethod
	public void afterMethod() {
		results = null;
	}

	@Test
	public void noError() throws Exception {
		ValidatableDummy dummy = new ValidatableDummy();
		dummy.validate(results);

		assertNotNull(results);
		assertEquals(0, results.getNumErrors());

		// no exception
		results.checkValidations();
	}

	@Test
	public void testErrors() {
		ValidatableDummy dummy = new ValidatableDummy();

		assertNotNull(results);
		assertEquals(0, results.getNumErrors());

		dummy.setGenerateError(true);
		dummy.validate(results);
		assertEquals(1, results.getNumErrors());
		assertEquals("error 0", results.toString());

		dummy.validate(results);
		assertEquals(2, results.getNumErrors());
		assertEquals("error 0" + ValidationResults.LINE_SEPARATOR + "error 1", results.toString());
	}

	@Test
	public void checkValidation() {
		results.addError("test");
		try {
			results.checkValidations();
			fail("no exception was thrown");
		} catch (ValidationException exc) {
			assertNotNull(exc.getValidationResults());
			assertTrue(results == exc.getValidationResults());
			assertEquals("Data validation failed:" + ValidationResults.LINE_SEPARATOR + "test", exc.getMessage());
		}
	}

	@Test
	public void addError() {
		results.addError("test");

		assertEquals(1, results.getNumErrors());
		assertEquals("test", results.toString());
	}

	@Test
	public void addFormatedError() {
		results.addError("%d %s %b", 1, "test", true);

		assertEquals(1, results.getNumErrors());
		assertEquals("1 test true", results.toString());
	}

	@Test
	public void validateNotNull() {
		results.validateNotNull(this, "this!");
		assertEquals(0, results.getNumErrors());
		assertEquals("", results.toString());

		results.validateNotNull(null, "upsa!");
		assertEquals(1, results.getNumErrors());
		assertEquals("upsa! is null", results.toString());
	}

	@Test
	public void validateMinLength() {
		for (int i = 0; i <= 4; i++) {
			results.validateMinLength("test", "name", i);
			assertEquals(0, results.getNumErrors());
			assertEquals("", results.toString());
		}

		results.validateMinLength("test", "name", 5);
		assertEquals(1, results.getNumErrors());
		assertEquals("name is too short (4), the minimum is 5", results.toString());
	}

	@Test
	public void validateMaxLength() {
		for (int i = 10; i >= 4; i--) {
			results.validateMaxLength("test", "name", i);
			assertEquals(0, results.getNumErrors());
			assertEquals("", results.toString());
		}

		results.validateMaxLength("test", "name", 3);
		assertEquals(1, results.getNumErrors());
		assertEquals("name is too long (4), the maximum is 3", results.toString());
	}
}
