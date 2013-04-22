/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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

import ch.bind.philib.validation.Validation;

/**
 * A simple implementation of a copy-on-write list.<br />
 * There are two modification methods: {@link #add(Object)} and {@link #remove(Object)}<br />
 * and one read method: {@link #getView()} <br />
 * Every modification through the {@code add} or {@code remove} methods will update the view. <br />
 * The view is shared among all clients and must therefore <b>not be written to</b>.<br/>
 * The modification methods are not optimized for speed since the intent of this cow-list is to guarantee fast reads.
 * @author Philipp Meinen
 */
public final class SimpleCowList<E> {

	private final Class<E> clazz;

	private final ArrayList<E> content = new ArrayList<E>(4);

	private int numRemovesSinceTrim;

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
		synchronized (content) {
			boolean update = content.add(e);
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
		synchronized (content) {
			boolean update = content.remove(e);
			if (update) {
				updateView();
				if (numRemovesSinceTrim > content.size()) {
					content.trimToSize();
					numRemovesSinceTrim = 0;
				} else {
					numRemovesSinceTrim++;
				}
			}
			return update;
		}
	}

	private void updateView() {
		int n = content.size();
		if (n == 0) {
			view = null;
		} else {
			view = content.toArray(mkArray(clazz, n));
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
