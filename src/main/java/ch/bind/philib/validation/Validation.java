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

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class Validation {

	private Validation() {
	}

	public static void notNegative(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("value must not be negative");
		}
	}

	public static void notNegative(int value, String message) {
		if (value < 0) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void notNegative(long value) {
		if (value < 0) {
			throw new IllegalArgumentException("value must not be negative");
		}
	}

	public static void notNegative(long value, String message) {
		if (value < 0) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void notNull(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("object must not be null");
		}
	}

	public static void notNull(Object obj, String message) {
		if (obj == null) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void isTrue(boolean value) {
		if (!value) {
			throw new IllegalArgumentException("value must be true");
		}
	}

	public static void isTrue(boolean value, String message) {
		if (!value) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void isFalse(boolean value) {
		if (value) {
			throw new IllegalArgumentException("value must be false");
		}
	}

	public static void isFalse(boolean value, String message) {
		if (value) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void notNullOrEmpty(Collection<?> value) {
		notNullOrEmpty(value, "null or empty collection provided");
	}

	public static void notNullOrEmpty(Collection<?> value, String message) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	public static <T> void notNullOrEmpty(T[] value) {
		notNullOrEmpty(value, "null or empty array provided");
	}

	public static <T> void notNullOrEmpty(T[] value, String message) {
		if (value == null || value.length == 0) {
			throw new IllegalArgumentException(message);
		}
	}
}
