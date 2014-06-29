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

package ch.bind.philib.util;

import ch.bind.philib.lang.ArrayUtil;
import ch.bind.philib.validation.Validation;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple implementation of a copy-on-write set.<br />
 * There are two modification methods: {@link #add(Object)} and {@link #remove(Object)}<br />
 * and two read methods: {@link #getView()} and {@link #isEmpty()}<br />
 * Every modification through the {@link #add(Object)} or {@link #remove(Object)} methods and a
 * return value of {@code true} will update the view. <br />
 * The view is shared among all clients and must therefore <b>not be modified!</b><br/>
 * The modification methods are not optimized for speed since the intent of this cow-list is to
 * guarantee fast reads.
 *
 * @author Philipp Meinen
 */
public final class CowSet<E> {

	private final Class<E> clazz;

	private final Set<E> content = new HashSet<E>();

	private volatile E[] empty;

	private volatile E[] view;

	public CowSet(Class<E> clazz) {
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
			}
			return update;
		}
	}

	private void updateView() {
		int n = content.size();
		view = (n == 0) ? null : ArrayUtil.toArray(clazz, content);
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

	public int size() {
		E[] v = view;
		return v == null ? 0 : v.length;
	}

	private E[] getEmpty() {
		E[] e = empty;
		if (e == null) {
			e = ArrayUtil.newArray(clazz, 0);
			empty = e;
		}
		return e;
	}
}
