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

package ch.bind.philib.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CompareUtilTest {

    @Test
    public void equalityObjObj() {
        Object a = "a";
        Object b = "b";

        boolean eq = CompareUtil.equals(a, b);
        assertFalse(eq);

        a = "b";
        eq = CompareUtil.equals(a, b);
        assertTrue(eq);

        b = "a";
        eq = CompareUtil.equals(a, b);
        assertFalse(eq);
    }

    @Test
    public void equalityNullNull() {
        Object a = null;
        Object b = null;

        boolean eq = CompareUtil.equals(a, b);
        assertTrue(eq);
    }

    @Test
    public void equalityObjNull() {
        Object a = "a";
        Object b = null;

        boolean eq = CompareUtil.equals(a, b);
        assertFalse(eq);
    }

    @Test
    public void equalityNullObj() {
        Object a = null;
        Object b = "b";

        boolean eq = CompareUtil.equals(a, b);
        assertFalse(eq);
    }

    @Test
    public void compareStringString() {
        String a = "a";
        String b = "b";

        int cmp = CompareUtil.compare(a, b);
        assertEquals(-1, cmp);

        a = "b";
        cmp = CompareUtil.compare(a, b);
        assertEquals(0, cmp);

        b = "a";
        cmp = CompareUtil.compare(a, b);
        assertEquals(1, cmp);
    }

    @Test
    public void compareStringNull() {
        String a = "a";
        String b = null;

        int cmp = CompareUtil.compare(a, b);
        assertEquals(1, cmp); // a > b
    }

    @Test
    public void compareNullString() {
        String a = null;
        String b = "b";

        int cmp = CompareUtil.compare(a, b);
        assertEquals(-1, cmp); // a < b
    }

    @Test
    public void compareNullNull() {
        String a = null;
        String b = null;

        int cmp = CompareUtil.compare(a, b);
        assertEquals(0, cmp); // a == b
    }

    @Test
    public void normalizeInt() {
        assertEquals(0, CompareUtil.normalize(0));
        for (int i = -1000; i < 0; i++) {
            assertEquals(-1, CompareUtil.normalize(i));
        }
        for (int i = 1; i <= 1000; i++) {
            assertEquals(1, CompareUtil.normalize(i));
        }
        assertEquals(-1, CompareUtil.normalize(Integer.MIN_VALUE));
        assertEquals(1, CompareUtil.normalize(Integer.MAX_VALUE));
    }

    @Test
    public void normalizeLong() {
        assertEquals(0L, CompareUtil.normalize(0L));
        for (long i = -1000; i < 0; i++) {
            assertEquals(-1L, CompareUtil.normalize(i));
        }
        for (long i = 1; i <= 1000; i++) {
            assertEquals(1L, CompareUtil.normalize(i));
        }
        assertEquals(-1L, CompareUtil.normalize(Long.MIN_VALUE));
        assertEquals(1L, CompareUtil.normalize(Long.MAX_VALUE));
    }
}
