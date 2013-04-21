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

package ch.bind.philib.msg;

import java.util.ArrayList;
import java.util.List;

import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */

public final class SimpleCowList<E> {

	private final Class<E> clazz;

	private final List<E> all = new ArrayList<E>();

	private volatile E[] empty;

	private volatile E[] view;

	public SimpleCowList(Class<E> clazz) {
		Validation.notNull(clazz);
		this.clazz = clazz;
	}

	public boolean add(E e) {
		if (e == null) {
			return false;
		}
		synchronized (all) {
			boolean update = all.add(e);
			if (update) {
				updateView();
			}
			return update;
		}
	}

	public boolean remove(E e) {
		if (e == null) {
			return false;
		}
		synchronized (all) {
			boolean update = all.remove(e);
			if (update) {
				updateView();
			}
			return update;
		}
	}

	private void updateView() {
		int n = all.size();
		if (n == 0) {
			view = null;
		} else {
			view = all.toArray(mkArray(clazz, n));
		}
	}

	/**
	 * @return the current content of the copy-on-write set ; do not modify!
	 */
	public E[] getView() {
		E[] v = view;
		if (v == null) {
			return getEmpty();
		}
		return v;
	}

	public boolean isEmpty() {
		E[] v = view;
		return v == null || v.length == 0;
	}

	private E[] getEmpty() {
		E[] e = empty;
		if (e == null) {
			e = mkArray(clazz, 0);
			empty = e;
		}
		return e;
	}

	private static <E> E[] mkArray(Class<E> clazz, int size) {
		return (E[]) java.lang.reflect.Array.newInstance(clazz, size);
	}
}
