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

package ch.bind.philib.concurrency;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import ch.bind.philib.lang.CompareUtil;

public final class LockGroup {

	private static final LockableComparator lockableComparator = new LockableComparator();

	private final Lockable[] objects;

	public LockGroup(final Lockable[] objects) {
		if (objects == null || objects.length == 0)
			throw new IllegalArgumentException("No lockables supplied");
		final int n = objects.length;
		this.objects = new Lockable[n];
		System.arraycopy(objects, 0, this.objects, 0, n);
		// FIXME: the array could contain the same lock twice! remove it? handle
		// it in lock/unlock?
		Arrays.sort(this.objects, lockableComparator);
	}

	public void lock() {
		for (Lockable l : objects) {
			l.lock();
		}
	}

	public void unlock() {
		for (Lockable l : objects) {
			l.unlock();
		}
	}

	private static final class LockableComparator implements Comparator<Lockable>, Serializable {

		private static final long serialVersionUID = 157755479046748535L;

		@Override
		public int compare(final Lockable o1, final Lockable o2) {
			final long diff = o1.getLockId() - o2.getLockId();
			return CompareUtil.normalize(diff);
		}
	}
}
