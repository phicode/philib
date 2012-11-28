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

package ch.bind.philib.cache.buf;

import ch.bind.philib.cache.buf.factory.BufferFactory;
import ch.bind.philib.lang.ArrayUtil;
import ch.bind.philib.pool.obj.ConcurrentBufferCache;
import ch.bind.philib.pool.obj.LinkedObjectCache;
import ch.bind.philib.pool.obj.NoopObjectCache;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class ByteArrayCache implements BufCache<byte[]> {

	@Override
	public final byte[] acquire() {
		return backend.acquire();
	}

	@Override
	public final void free(byte[] e) {
		backend.free(e);
	}

	@Override
	public final Stats getCacheStats() {
		return backend.getCacheStats();
	}
	
	
//	private ByteArrayCache(BufferCache<byte[]> cache) {
//		super(cache);
//	}
//
//	public static ByteArrayCache createSimple(int bufferSize) {
//		return createSimple(bufferSize, DEFAULT_NUM_BUFFERS);
//	}
//
//	public static ByteArrayCache createSimple(int bufferSize, int maxEntries) {
//		BufferFactory<byte[]> factory = createFactory(bufferSize);
//		BufferCache<byte[]> cache = new LinkedObjectCache<byte[]>(factory, maxEntries);
//		return new ByteArrayCache(cache);
//	}
//
//	public static ByteArrayCache createConcurrent(int bufferSize, int maxEntries) {
//		BufferFactory<byte[]> factory = createFactory(bufferSize);
//		BufferCache<byte[]> cache = new ConcurrentBufferCache<byte[]>(factory, maxEntries);
//		return new ByteArrayCache(cache);
//	}
//
//	public static ByteArrayCache createConcurrent(int bufferSize, int maxEntries, int bufferBuckets) {
//		BufferFactory<byte[]> factory = createFactory(bufferSize);
//		BufferCache<byte[]> cache = new ConcurrentBufferCache<byte[]>(factory, maxEntries, bufferBuckets);
//		return new ByteArrayCache(cache);
//	}
//
//	public static ByteArrayCache createNoop(int bufferSize) {
//		BufferFactory<byte[]> factory = createFactory(bufferSize);
//		BufferCache<byte[]> cache = new NoopObjectCache<byte[]>(factory);
//		return new ByteArrayCache(cache);
//	}
//
//	public static BufferFactory<byte[]> createFactory(int bufferSize) {
//		return new ByteArrayFactory(bufferSize);
//	}
}
