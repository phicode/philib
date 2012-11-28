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

import java.nio.ByteBuffer;

import ch.bind.philib.cache.buf.factory.BufferFactory;
import ch.bind.philib.lang.ArrayUtil;
import ch.bind.philib.pool.obj.ConcurrentBufferCache;
import ch.bind.philib.pool.obj.LinkedObjectCache;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class ByteBufferCache{ //extends BufferCacheImplSpecificCacheBase<ByteBuffer> {

	private final BufCache<ByteBuffer> backend;
	
	@Override
	public final ByteBuffer acquire() {
		return backend.acquire();
	}

	@Override
	public final void free(ByteBuffer e) {
		backend.free(e);
	}

	@Override
	public final Stats getCacheStats() {
		return backend.getCacheStats();
	}
	
//	private ByteBufferCache(BufferCache<ByteBuffer> cache) {
//		super(cache);
//	}
//
//	public static ByteBufferCache createSimple(int bufferSize, int maxEntries) {
//		BufferFactory<ByteBuffer> factory = createFactory(bufferSize);
//		BufferCache<ByteBuffer> cache = new LinkedObjectCache<ByteBuffer>(factory, maxEntries);
//		return new ByteBufferCache(cache);
//	}
//
//	public static ByteBufferCache createScalable(int bufferSize, int maxEntries) {
//		BufferFactory<ByteBuffer> factory = createFactory(bufferSize);
//		BufferCache<ByteBuffer> cache = new ConcurrentBufferCache<ByteBuffer>(factory, maxEntries);
//		return new ByteBufferCache(cache);
//	}
//
//	public static ByteBufferCache createScalable(int bufferSize, int maxEntries, int bufferBuckets) {
//		BufferFactory<ByteBuffer> factory = createFactory(bufferSize);
//		BufferCache<ByteBuffer> cache = new ConcurrentBufferCache<ByteBuffer>(factory, maxEntries, bufferBuckets);
//		return new ByteBufferCache(cache);
//	}
//
//	public static BufferFactory<ByteBuffer> createFactory(int bufferSize) {
//		return new ByteBufferFactory(bufferSize);
//	}

}
