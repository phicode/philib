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

package ch.bind.philib.data;

/**
 * Provides helper methods for hash-code generation.<br/>
 * Hash methods for handling all primitive types as well as <code>Object</code>s
 * are provided.<br/>
 * 
 * <b>Usage:</b>
 * 
 * <pre>
 * &#64;Override
 * public int hashCode() {
 *     int hash = HashUtil.start(field1);
 *     hash = HashUtil.next(field2);
 *     ...
 *     hash = HashUtil.next(fieldN);
 *     return hash;
 * }
 * </pre>
 * 
 * Note: fields may be null.
 * 
 * @author Philipp Meinen
 */
public final class HashUtil {

    private HashUtil() {
    }

    private static final int HASH_PRIME_START = 17;
    private static final int HASH_PRIME_STEP = 31;

    public static final int start(final Object obj) {
        return next(HASH_PRIME_START, obj);
    }

    public static final int next(int hash, final Object obj) {
        return hash * HASH_PRIME_STEP + ((obj == null) ? 0 : obj.hashCode());
    }

    public static final int start(final boolean value) {
        return next(HASH_PRIME_START, value);
    }

    public static final int next(int hash, final boolean value) {
        return hash * HASH_PRIME_STEP + (value ? 1 : 0);
    }

    public static final int start(final byte value) {
        return next(HASH_PRIME_START, value);
    }

    public static final int next(int hash, final byte value) {
        return hash * HASH_PRIME_STEP + (int) value;
    }

    public static final int start(final char value) {
        return next(HASH_PRIME_START, value);
    }

    public static final int next(int hash, final char value) {
        return hash * HASH_PRIME_STEP + value;
    }

    public static final int start(final short value) {
        return next(HASH_PRIME_START, value);
    }

    public static final int next(int hash, final short value) {
        return hash * HASH_PRIME_STEP + value;
    }

    public static final int start(final int value) {
        return next(HASH_PRIME_START, value);
    }

    public static final int next(int hash, final int value) {
        return hash * HASH_PRIME_STEP + value;
    }

    public static final int start(final long value) {
        return next(HASH_PRIME_START, value);
    }

    public static final int next(int hash, final long value) {
        return hash * HASH_PRIME_STEP + (int) (value ^ (value >>> 32));
    }

    public static final int start(final float value) {
        return next(HASH_PRIME_START, value);
    }

    public static final int next(final int hash, final float value) {
        return next(hash, Float.floatToIntBits(value));
    }

    public static final int start(final double value) {
        return next(HASH_PRIME_START, value);
    }

    public static final int next(final int hash, final double value) {
        return next(hash, Double.doubleToLongBits(value));
    }
}
