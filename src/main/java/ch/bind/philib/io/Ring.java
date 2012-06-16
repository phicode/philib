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

public class Ring<T> {

	private static final int INITIAL_RING_LEN = 4;

	private static final int RING_LEN_ENHANCING_FACTOR = 2;

	private int off;

	private int size;

	private Object[] ring;

	public void addBack(T value) {
		if (value == null) {
			return;
		}
		ensureRingSpace();
		int addPos = (off + size) % ring.length;
		ring[addPos] = value;
		size++;
	}

	public void addFront(T value) {
		if (value == null) {
			return;
		}
		ensureRingSpace();
		off--;
		if (off == -1) {
			off = ring.length - 1;
		}
		ring[off] = value;
		size++;
	}

	@SuppressWarnings("unchecked")
	public T poll() {
		if (size == 0) {
			return null;
		} else {
			Object value = ring[off];
			ring[off] = null;
			off = (off + 1) % ring.length;
			size--;
			return (T) value;
		}
	}

	public T pollNext(T value) {
		if (size == 0) {
			return value;
		} else {
			T rv = poll();
			if (value != null) {
				addBack(value);
			}
			return rv;
		}
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	public void clear() {
		off = 0;
		size = 0;
		Arrays.fill(ring, null);
	}

	private void ensureRingSpace() {
		if (ring == null) {
			ring = new Object[INITIAL_RING_LEN];
		} else {
			if (size == ring.length) {
				int newLen = ring.length * RING_LEN_ENHANCING_FACTOR;
				if (newLen < 0) {
					// TODO
					System.out.println("overflow!");
					newLen = Integer.MAX_VALUE;
				}
				Object[] newRing = new Object[newLen];
				System.arraycopy(ring, 0, newRing, 0, ring.length);
				this.ring = newRing;
			}
		}
	}
}
