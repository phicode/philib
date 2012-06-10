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

public final class BufferQueue {

	private final long maxBufSize;

	private final Ring<byte[]> ring = new Ring<byte[]>();

	private long curBufSize;

	public BufferQueue() {
		this(Long.MAX_VALUE);
	}

	public BufferQueue(long maxBufSize) {
		this.maxBufSize = maxBufSize;
	}

	public boolean canOffer(byte[] data) {
		if (data == null || data.length == 0) {
			return true;
		}
		else {
			long newSize = data.length + curBufSize;
			return newSize <= maxBufSize;
		}
	}

	public boolean offer(byte[] data) {
		if (data == null || data.length == 0) {
			return true;
		}
		else {
			long newSize = data.length + curBufSize;
			if (newSize > maxBufSize) {
				return false;
			}
			else {
				ring.addBack(data);
				curBufSize = newSize;
				return true;
			}
		}
	}

	public boolean offerFront(byte[] data) {
		if (data == null || data.length == 0) {
			return true;
		}
		else {
			long newSize = data.length + curBufSize;
			if (newSize > maxBufSize) {
				return false;
			}
			else {
				ring.addFront(data);
				curBufSize = newSize;
				return true;
			}
		}
	}

	public byte[] poll() {
		if (curBufSize == 0) {
			return null;
		}
		else {
			byte[] value = ring.poll();
			assert (value != null);
			curBufSize -= value.length;
			return value;
		}
	}

	public long size() {
		return curBufSize;
	}

	public boolean isEmpty() {
		return curBufSize == 0;
	}
}
