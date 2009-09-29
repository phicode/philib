package ch.bind.philib.validation;

public class ValidationException extends Exception {

	private final ValidationResults results;

	public ValidationException(ValidationResults results) {
		this.results = results;
	}

	public ValidationResults getValidationResults() {
		return results;
	}

	@Override
	public String getMessage() {
		return "Data validation failed: " + ValidationResults.LINE_SEPARATOR
				+ results.toString();
	}
}
