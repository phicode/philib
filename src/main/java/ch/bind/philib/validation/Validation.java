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

import java.util.Collection;
import java.util.Map;

/**
 * @author Philipp Meinen
 */
public abstract class Validation {

	protected Validation() {
	}

	public static int notNegative(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("value must not be negative");
		}
		return value;
	}

	public static int notNegative(int value, String message) {
		if (value < 0) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public static long notNegative(long value) {
		if (value < 0) {
			throw new IllegalArgumentException("value must not be negative");
		}
		return value;
	}

	public static long notNegative(long value, String message) {
		if (value < 0) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public static <T> T notNull(T obj) {
		if (obj == null) {
			throw new IllegalArgumentException("object must not be null");
		}
		return obj;
	}

	public static <T> T notNull(T obj, String message) {
		if (obj == null) {
			throw new IllegalArgumentException(message);
		}
		return obj;
	}

	public static boolean isTrue(boolean value) {
		if (!value) {
			throw new IllegalArgumentException("value must be true");
		}
		return true;
	}

	public static boolean isTrue(boolean value, String message) {
		if (!value) {
			throw new IllegalArgumentException(message);
		}
		return true;
	}

	public static boolean isFalse(boolean value) {
		if (value) {
			throw new IllegalArgumentException("value must be false");
		}
		return false;
	}

	public static boolean isFalse(boolean value, String message) {
		if (value) {
			throw new IllegalArgumentException(message);
		}
		return false;
	}

	public static <T extends CharSequence> T notNullOrEmpty(T value) {
		return notNullOrEmpty(value, "null or empty char sequence provided");
	}

	public static <T extends CharSequence> T notNullOrEmpty(T value, String message) {
		if (value == null || value.length() == 0) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public static <T extends Collection<?>> T notNullOrEmpty(T value) {
		return notNullOrEmpty(value, "null or empty collection provided");
	}

	public static <T extends Collection<?>> T notNullOrEmpty(T value, String message) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public static <T extends Map<?, ?>> T notNullOrEmpty(T value) {
		return notNullOrEmpty(value, "null or empty collection provided");
	}

	public static <T extends Map<?, ?>> T notNullOrEmpty(T value, String message) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public static <T> T[] notNullOrEmpty(T[] values) {
		return notNullOrEmpty(values, "null or empty array provided");
	}

	public static <T> T[] notNullOrEmpty(T[] values, String message) {
		if (values == null || values.length == 0) {
			throw new IllegalArgumentException(message);
		}
		return values;
	}

	public static <T> T[] noNullValues(T[] values) {
		return noNullValues(values, "array contains null values");
	}

	public static <T> T[] noNullValues(T[] values, String message) {
		notNull(values);
		for (T v : values) {
			notNull(v, message);
		}
		return values;
	}

	/**
	 * Verifies thats a all arrays of a two-dimensional array are of equal length.
	 * @param matrix the two dimensional array to be verified.
	 */
	public static void equalLengths(Object[][] matrix) {
		equalLengths(matrix, "multi dimensional array contains sub-arrays of different length");
	}
	/**
	 * Verifies thats a all arrays of a two-dimensional array are of equal length.
	 * @param matrix the two dimensional array to be verified.
	 * @param message the error to throw upon encountering a validation missmatch.
	 */
	public static void equalLengths(Object[][] matrix, String message) {
		Validation.notNull(matrix);
		if (matrix.length < 2) {
			return;
		}
		int l = matrix[0].length;
		for (int i = 1; i < matrix.length; i++) {
			if (matrix[i].length != l) {
				throw new IllegalArgumentException(message);
			}
		}
	}
}
