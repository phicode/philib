/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.util;

import ch.bind.philib.lang.CompareUtil;

import java.util.Comparator;

/**
 * A key value pair who's key is of type long.
 *
 * @author Philipp Meinen
 */
public interface LongPair<T> {

	long getKey();

	T getValue();

	public static final Comparator<LongPair<?>> KEY_COMPARATOR = new Comparator<LongPair<?>>() {

		@Override
		public int compare(LongPair<?> a, LongPair<?> b) {
			if (a == null || b == null) {
				throw new NullPointerException("LongPair.KEY_COMPARATOR does not support null-values");
			}
			return CompareUtil.diff(a.getKey(), b.getKey());
		}
	};
}
