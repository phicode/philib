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

import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.PoolStats;
import ch.bind.philib.pool.manager.ByteArrayManager;
import ch.bind.philib.pool.object.ConcurrentPool;
import ch.bind.philib.pool.object.LeakyPool;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class ByteArrayPool implements Pool<byte[]> {

	private final Pool<byte[]> backend;

	public ByteArrayPool(Pool<byte[]> backend) {
		Validation.notNull(backend);
		this.backend = backend;
	}

	@Override
	public final byte[] take() {
		return backend.take();
	}

	@Override
	public final void recycle(byte[] value) {
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

	public static ByteArrayPool create(int bufferSize, int maxEntries) {
		ByteArrayManager manager = new ByteArrayManager(bufferSize);
		return new ByteArrayPool(new LeakyPool<byte[]>(manager, maxEntries));
	}

	public static ByteArrayPool create(int bufferSize, int maxEntries, int concurrencyLevel) {
		ByteArrayManager manager = new ByteArrayManager(bufferSize);
		if (concurrencyLevel < 2) {
			return new ByteArrayPool(new LeakyPool<byte[]>(manager, maxEntries));
		}
		return new ByteArrayPool(new ConcurrentPool<byte[]>(manager, maxEntries, true, concurrencyLevel));
	}
}
