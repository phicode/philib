package ch.bind.philib.validation;

import java.util.LinkedList;
import java.util.List;

public final class ValidationResults {

	private final List<String> errors;

	public ValidationResults() {
		errors = new LinkedList<String>();
	}

	public int getNumErrors() {
		return errors.size();
	}

	public void addError(final String message) {
		errors.add(message);
	}

	public void addError(final String format, Object... args) {
		errors.add(String.format(format, args));
	}

	public boolean validateNotNull(final Object testNull, final String name) {
		if (testNull == null) {
			addError("%s is null", name);
			return false;
		}
		return true;
	}

	public boolean validateMaxLength(final String testStr, final String name,
			final int maxLength) {
		if (testStr.length() > maxLength) {
			addError("%s is too long (%d), the maximum is %d", name, testStr
					.length(), maxLength);
			return false;
		}
		return true;
	}

	public boolean validateMinLength(final String testStr, final String name,
			final int minLength) {
		if (testStr.length() > minLength) {
			addError("%s is too short (%d), the miniumum is %d", name, testStr
					.length(), minLength);
			return false;
		}
		return true;
	}

	public void checkValidations() throws ValidationException {
		if (errors.size() > 0)
			throw new ValidationException(this);
	}

	static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int num = 1;
		for (String msg : errors) {
			if (num > 1)
				sb.append(LINE_SEPARATOR);
			sb.append(msg);
			num++;
		}
		return sb.toString();
	}
}
