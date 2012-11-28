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

package ch.bind.philib.cache.buffercache;

import ch.bind.philib.cache.buffercache.impl.BufferFactory;
import ch.bind.philib.cache.buffercache.impl.ConcurrentBufferCache;
import ch.bind.philib.cache.buffercache.impl.LinkedObjectCache;
import ch.bind.philib.cache.buffercache.impl.NoopObjectCache;
import ch.bind.philib.lang.ArrayUtil;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class ByteArrayCache extends SpecificCacheBase<byte[]> {

	public static final int DEFAULT_BUFFER_SIZE = 8192;

	// 1 mb of buffers with the default buffer size of 8k
	public static final int DEFAULT_NUM_BUFFERS = 128;

	private ByteArrayCache(BufferCache<byte[]> cache) {
		super(cache);
	}

	public static ByteArrayCache createSimple(int bufferSize) {
		return createSimple(bufferSize, DEFAULT_NUM_BUFFERS);
	}

	public static ByteArrayCache createSimple(int bufferSize, int maxEntries) {
		BufferFactory<byte[]> factory = createFactory(bufferSize);
		BufferCache<byte[]> cache = new LinkedObjectCache<byte[]>(factory, maxEntries);
		return new ByteArrayCache(cache);
	}

	public static ByteArrayCache createConcurrent(int bufferSize, int maxEntries) {
		BufferFactory<byte[]> factory = createFactory(bufferSize);
		BufferCache<byte[]> cache = new ConcurrentBufferCache<byte[]>(factory, maxEntries);
		return new ByteArrayCache(cache);
	}

	public static ByteArrayCache createConcurrent(int bufferSize, int maxEntries, int bufferBuckets) {
		BufferFactory<byte[]> factory = createFactory(bufferSize);
		BufferCache<byte[]> cache = new ConcurrentBufferCache<byte[]>(factory, maxEntries, bufferBuckets);
		return new ByteArrayCache(cache);
	}

	public static ByteArrayCache createNoop(int bufferSize) {
		BufferFactory<byte[]> factory = createFactory(bufferSize);
		BufferCache<byte[]> cache = new NoopObjectCache<byte[]>(factory);
		return new ByteArrayCache(cache);
	}

	public static BufferFactory<byte[]> createFactory(int bufferSize) {
		return new ByteArrayFactory(bufferSize);
	}

	public static final class ByteArrayFactory implements BufferFactory<byte[]> {

		private final int bufferSize;

		public ByteArrayFactory(int bufSize) {
			this.bufferSize = bufSize;
		}

		@Override
		public byte[] create() {
			return new byte[bufferSize];
		}

		@Override
		public boolean prepareForReuse(byte[] e) {
			if (e.length == bufferSize) {
				ArrayUtil.memsetZero(e);
				return true;
			}
			return false;
		}
	}
}
