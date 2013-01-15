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

package ch.bind.philib.pool.buffer;

import java.nio.ByteBuffer;

import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.PoolStats;
import ch.bind.philib.pool.manager.ByteBufferManager;
import ch.bind.philib.pool.object.ConcurrentPool;
import ch.bind.philib.pool.object.SoftRefPool;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class ByteBufferPool implements Pool<ByteBuffer> {

	private final Pool<ByteBuffer> backend;

	public ByteBufferPool(Pool<ByteBuffer> backend) {
		Validation.notNull(backend);
		this.backend = backend;
	}

	@Override
	public final ByteBuffer take() {
		return backend.take();
	}

	@Override
	public final void recycle(ByteBuffer value) {
		backend.recycle(value);
	}

	@Override
	public final PoolStats getPoolStats() {
		return backend.getPoolStats();
	}

	@Override
	public int getNumPooled() {
		return backend.getNumPooled();
	}

	@Override
	public void clear() {
		backend.clear();
	}

	public static ByteBufferPool create(int bufferSize, int maxEntries) {
		ByteBufferManager manager = new ByteBufferManager(bufferSize);
		return new ByteBufferPool(new SoftRefPool<ByteBuffer>(manager, maxEntries));
	}

	public static ByteBufferPool create(int bufferSize, int maxEntries, int concurrencyLevel) {
		ByteBufferManager manager = new ByteBufferManager(bufferSize);
		if (concurrencyLevel < 2) {
			return new ByteBufferPool(new SoftRefPool<ByteBuffer>(manager, maxEntries));
		}
		return new ByteBufferPool(new ConcurrentPool<ByteBuffer>(manager, maxEntries, true, concurrencyLevel));
	}
}
