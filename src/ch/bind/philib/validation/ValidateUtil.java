package ch.bind.philib.validation;

import java.util.Collection;

public final class ValidateUtil {

	private ValidateUtil() {
	}

	public static final ValidationResults validate(final Validatable validatable) {
		final ValidationResults results = new ValidationResults();
		validatable.validate(results); // NPE is intended!
		return results;
	}

	public static final ValidationResults validate(
			final Collection<Validatable> validatables) {
		final ValidationResults results = new ValidationResults();
		for (Validatable validatable : validatables) {
			validatable.validate(results); // NPE is intended!
		}
		return results;
	}

	public static final ValidationResults validate(
			final Validatable[] validatables) {
		final ValidationResults results = new ValidationResults();
		for (Validatable validatable : validatables) {
			validatable.validate(results); // NPE is intended!
		}
		return results;
	}
}
