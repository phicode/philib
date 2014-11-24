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

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author Philipp Meinen
 */
public final class ValidationResults implements Serializable {

	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final long serialVersionUID = 8465822377127857280L;
	private final LinkedList<String> errors;

	public ValidationResults() {
		errors = new LinkedList<>();
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

	public boolean validateMaxLength(final String testStr, final String name, final int maxLength) {
		if (testStr.length() > maxLength) {
			addError("%s is too long (%d), the maximum is %d", name, testStr.length(), maxLength);
			return false;
		}
		return true;
	}

	public boolean validateMinLength(final String testStr, final String name, final int minLength) {
		if (testStr.length() < minLength) {
			addError("%s is too short (%d), the minimum is %d", name, testStr.length(), minLength);
			return false;
		}
		return true;
	}

	public void checkValidations() throws ValidationException {
		if (errors.size() > 0)
			throw new ValidationException(this);
	}

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
