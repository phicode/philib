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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.pool.impl.ObjectFactory;
import ch.bind.philib.pool.impl.ObjectPool;
import ch.bind.philib.pool.impl.ObjectPoolImpl;

public final class BufferPool extends Bar<byte[]> {

	public static final int DEFAULT_BUFFER_SIZE = 8192;

	// 1 mb of buffers with the default buffer size of 8k
	public static final int DEFAULT_NUM_BUFFERS = 128;

	private final ByteArrayFactory factory;

	private final ObjectPool<byte[]> pool;

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
		this.factory = new ByteArrayFactory(bufferSize);
		this.pool = pool;
	}

	public long getNumCreates() {
		return factory.creates.get();
	}

	@Override
	public byte[] get() {
		return pool.get(factory);
	}

	@Override
	public void release(byte[] buf) {
		pool.release(factory, buf);
	}

	private static final class ByteArrayFactory implements ObjectFactory<byte[]> {

		private final int bufSize;

		private final AtomicLong creates = new AtomicLong();

		public ByteArrayFactory(int bufSize) {
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

		@Override
		public void released(byte[] e) {
			Arrays.fill(e, (byte) 0);
		}

		@Override
		public boolean canRelease(byte[] e) {
			return e.length == bufSize;
		}
	}
}
