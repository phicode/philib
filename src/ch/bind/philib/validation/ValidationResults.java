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

	public void addError(String message) {
		errors.add(message);
	}

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

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
