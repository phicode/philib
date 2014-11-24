/*
 * Copyright (c) 2014 Philipp Meinen <philipp@bind.ch>
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
 * {@code ClonerNoop} is an implementation of the {@link Cloner} interface which returns the same object (or {@code null}).
 *
 * @author Philipp Meinen
 */
public final class ClonerNoop<T> implements Cloner<T> {

	public static final Cloner<?> INSTANCE = new ClonerNoop<>();

	@Override
	public T clone(T obj) {
		return obj;
	}

	@SuppressWarnings("unchecked")
	public static <T> Cloner<T> get() {
		return (Cloner<T>) INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public static <T> Cloner<T> getIfNull(Cloner<T> cloner) {
		return cloner == null ? (Cloner<T>) INSTANCE : cloner;
	}
}
