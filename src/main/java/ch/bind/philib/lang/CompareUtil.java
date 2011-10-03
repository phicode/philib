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

package ch.bind.philib.lang;

/**
 * Provides helper methods for object comparison.
 * 
 * @author Philipp Meinen
 */
public final class CompareUtil {

    private CompareUtil() {
    }

    public static boolean equals(final Object o1, final Object o2) {
        if (o1 == o2)
            return true;
        if (o1 == null) {
            // o2 is not null -> not equal
            return false;
        } else {
            if (o2 == null) {
                return false;
            } else {
                return o1.equals(o2);
            }
        }
    }

    public static <T> int compare(final Comparable<T> o1, final T o2) {
        if (o1 == o2)
            return 0;
        if (o1 == null) {
            // o2 is not null
            return -1; // o1 < o2
        } else {
            if (o2 == null) {
                return 1; // o1 > o2
            } else {
                return o1.compareTo(o2);
            }
        }
    }

    public static int compareBool(boolean a, boolean b) {
		return (a == b ? 0 : (a ? 1 : -1));
	}
    
    public static final int normalize(int diff) {
        return diff < 0 ? -1 : (diff == 0 ? 0 : 1);
    }

    public static final int normalize(long diff) {
        return (diff < 0 ? -1 : (diff == 0 ? 0 : 1));
    }
}
