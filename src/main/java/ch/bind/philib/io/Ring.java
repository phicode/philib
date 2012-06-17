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

package ch.bind.philib.io;

import java.util.Arrays;

import ch.bind.philib.validation.Validation;

public class Ring<T> {

	private static final int INITIAL_RING_LEN = 4;

	private static final int RING_LEN_ENHANCING_FACTOR = 2;

	private int off;

	private int size;

	private Object[] ring;

	public void addBack(final T value) {
		assert (size >= 0 && (ring == null || size <= ring.length));
		if (value == null) {
			return;
		}
		ensureRingSpace();
		int addPos = (off + size) % ring.length;
		ring[addPos] = value;
		size++;
	}

	public void addFront(final T value) {
		assert (size >= 0 && (ring == null || size <= ring.length));
		if (value == null) {
			return;
		}
		ensureRingSpace();
		off = off > 0 ? off - 1 : ring.length - 1;
		Validation.isTrue(off >= 0 && off < ring.length);
		ring[off] = value;
		size++;
	}

	@SuppressWarnings("unchecked")
	public T poll() {
		assert (size >= 0 && (ring == null || size <= ring.length));
		if (size == 0) {
			return null;
		} else {
			Object value = ring[off];
			ring[off] = null;
			off = (off + 1) % ring.length;
			size--;
			assert (value != null);
			return (T) value;
		}
	}

	public T pollNext(final T value) {
		assert (size >= 0 && (ring == null || size <= ring.length));
		if (size == 0) {
			return value;
		} else {
			final T rv = poll();
			if (value != null) {
				addBack(value);
			}
			assert (rv != null);
			return rv;
		}
	}

	public boolean isEmpty() {
		assert (size >= 0 && (ring == null || size <= ring.length));
		return size == 0;
	}

	public int size() {
		assert (size >= 0 && (ring == null || size <= ring.length));
		return size;
	}

	public void clear() {
		off = 0;
		size = 0;
		ring = null;
	}

	private void ensureRingSpace() {
		if (ring == null) {
			ring = new Object[INITIAL_RING_LEN];
		} else {
			final int l = ring.length;
			if (size == l) {
				int newLen = l * RING_LEN_ENHANCING_FACTOR;
				if (newLen < 0) {
					// TODO
					System.err.println("overflow!");
					newLen = Integer.MAX_VALUE;
				}
				Object[] newRing = new Object[newLen];
				if (off == 0) {
					System.arraycopy(ring, 0, newRing, 0, l);
				} else {
					int numToEnd = l - off;
					// from off to the end of the array
					System.arraycopy(ring, off, newRing, 0, numToEnd);
					// from the start of the array to before the off
					System.arraycopy(ring, 0, newRing, numToEnd, off);
					off = 0;
				}
				this.ring = newRing;
			}
		}
	}
}
