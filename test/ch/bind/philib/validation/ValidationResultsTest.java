package ch.bind.philib.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValidationResultsTest {

	private ValidationResults results;
	
	@Before
	public void setup() {
		results = new ValidationResults();
	}
	
	@After
	public void cleanup() {
		results=null;
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
		assertEquals("error 0" + ValidationResults.LINE_SEPARATOR + "error 1",
				results.toString());
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
			assertEquals("Data validation failed:"
					+ ValidationResults.LINE_SEPARATOR + "test", exc
					.getMessage());
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
		assertEquals("name is too short (4), the minimum is 5", results
				.toString());
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
		assertEquals("name is too long (4), the maximum is 3", results
				.toString());
	}
}
