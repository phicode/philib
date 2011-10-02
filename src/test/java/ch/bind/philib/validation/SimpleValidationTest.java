/*
 * Copyright (c) 2006-2009 Philipp Meinen <philipp@bind.ch>
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

import static junit.framework.Assert.fail;

import org.junit.Test;

public class SimpleValidationTest {

    @Test
    public void isFalse() {
        try {
            SimpleValidation.isFalse(false);
            SimpleValidation.isFalse(false, "foo");
        } catch (IllegalArgumentException e) {
            fail("should not throw");
        }
        try {
            SimpleValidation.isFalse(true);
            fail("should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            SimpleValidation.isFalse(true, "foo");
            fail("should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void isTrue() {
        try {
            SimpleValidation.isTrue(true);
            SimpleValidation.isTrue(true, "foo");
        } catch (IllegalArgumentException e) {
            fail("should not throw");
        }
        try {
            SimpleValidation.isTrue(false);
            fail("should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            SimpleValidation.isTrue(false, "foo");
            fail("should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void notNegative() {
        try {
            SimpleValidation.notNegative(0);
            SimpleValidation.notNegative(0, "foo");

            SimpleValidation.notNegative(0L);
            SimpleValidation.notNegative(0L, "foo");

            SimpleValidation.notNegative(Integer.MAX_VALUE);
            SimpleValidation.notNegative(Integer.MAX_VALUE, "foo");

            SimpleValidation.notNegative(Long.MAX_VALUE);
            SimpleValidation.notNegative(Long.MAX_VALUE, "foo");
        } catch (IllegalArgumentException e) {
            fail("should not throw");
        }
        try {
            SimpleValidation.notNegative(-1);
            fail("should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            SimpleValidation.notNegative(-1, "foo");
            fail("should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void notNull() {
        try {
            SimpleValidation.notNull("");
            SimpleValidation.notNull("", "foo");
        } catch (IllegalArgumentException e) {
            fail("should not throw");
        }
        try {
            SimpleValidation.notNull(null);
            fail("should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            SimpleValidation.notNull(null, "foo");
            fail("should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
