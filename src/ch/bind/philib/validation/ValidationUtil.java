package ch.bind.philib.validation;

import java.util.Collection;

public final class ValidationUtil {

	private ValidationUtil() {
	}

	public static final ValidationResults validate(final Validatable validatable) {
		final ValidationResults results = new ValidationResults();
		validatable.validate(results); // NPE is intended!
		return results;
	}

	public static final <T extends Validatable> ValidationResults validate(
			final Collection<T> validatables) {
		final ValidationResults results = new ValidationResults();
		for (Validatable validatable : validatables) {
			validatable.validate(results); // NPE is intended!
		}
		return results;
	}

	public static final <T extends Validatable> ValidationResults validate(
			final T[] validatables) {
		final ValidationResults results = new ValidationResults();
		for (Validatable validatable : validatables) {
			validatable.validate(results); // NPE is intended!
		}
		return results;
	}
}
