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

import java.nio.ByteBuffer;

public final class BlockingByteBufferQueue {

	private Ring<ByteBuffer> ring = new Ring<ByteBuffer>();
	private final int maxSizeBeforeBlocking;
	private int size;

	public BlockingByteBufferQueue(int maxSizeBeforeBlocking) {
		this.maxSizeBeforeBlocking = maxSizeBeforeBlocking;
	}

	public synchronized void addOrWait(ByteBuffer data) throws InterruptedException {
		final int rem = data.remaining();
		while ((size + rem) > maxSizeBeforeBlocking) {
			wait();
		}
		size += rem;
		ring.addBack(data);
	}

	public synchronized void addFront(ByteBuffer data) {
		size += data.remaining();
		ring.addFront(data);
	}

	public synchronized ByteBuffer poll() {
		ByteBuffer data = ring.poll();
		if (data != null) {
			size -= data.remaining();
			notifyAll();
		}
		return data;
	}
}
