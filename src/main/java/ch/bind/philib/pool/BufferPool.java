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
package ch.bind.philib.pool;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class BufferPool {

	public static final int DEFAULT_BUFFER_SIZE = 8192;

	// 1 mb of buffers with the default buffer size of 8k
	public static final int DEFAULT_NUM_BUFFERS = 128;

	private final int bufSize;
	private final ByteArrayObjectFactory factory;

	private final ObjectPool<byte[]> pool;

	private static final class ByteArrayObjectFactory implements ObjectFactory<byte[]> {

		private final int bufSize;
		private final AtomicLong creates = new AtomicLong();

		public ByteArrayObjectFactory(int bufSize) {
			this.bufSize = bufSize;
		}

		@Override
		public void destroy(byte[] e) {
		}

		@Override
		public byte[] create() {
			creates.incrementAndGet();
			return new byte[bufSize];
		}
	}

	public BufferPool() {
		this(DEFAULT_BUFFER_SIZE, DEFAULT_NUM_BUFFERS);
	}

	public BufferPool(int bufferSize) {
		this(bufferSize, DEFAULT_NUM_BUFFERS);
	}

	public BufferPool(int bufferSize, int maxBuffers) {
		this(bufferSize, new ObjectPoolImpl<byte[]>(maxBuffers));
	}

	public BufferPool(int bufferSize, ObjectPool<byte[]> pool) {
		this.bufSize = bufferSize;
		this.factory = new ByteArrayObjectFactory(bufferSize);
		this.pool = pool;
	}

	public long getNumCreates() {
		return factory.creates.get();
	}

	public byte[] get() {
		return pool.get(factory);
	}

	public void release(byte[] buf) {
		// discard buffers which do not have the right size
		if (buf != null && buf.length == bufSize) {
			pool.release(factory, buf);
		}
	}
}
