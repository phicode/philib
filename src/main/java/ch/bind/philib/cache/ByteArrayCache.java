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

package ch.bind.philib.cache;

import java.util.Arrays;

import ch.bind.philib.cache.impl.LinkedObjectCache;
import ch.bind.philib.cache.impl.NoopObjectCache;
import ch.bind.philib.cache.impl.ObjectFactory;
import ch.bind.philib.cache.impl.ScalableObjectCache;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class ByteArrayCache extends BufferCacheBase<byte[]> {

	public static final int DEFAULT_BUFFER_SIZE = 8192;

	// 1 mb of buffers with the default buffer size of 8k
	public static final int DEFAULT_NUM_BUFFERS = 128;

	private final ObjectFactory<byte[]> factory;

	private ByteArrayCache(ObjectCache<byte[]> cache, ObjectFactory<byte[]> factory) {
		super(cache);
		this.factory = factory;
	}

	public static ByteArrayCache createSimple(int bufferSize) {
		ObjectFactory<byte[]> factory = createFactory(bufferSize);
		ObjectCache<byte[]> cache = new LinkedObjectCache<byte[]>(factory, DEFAULT_NUM_BUFFERS);
		return new ByteArrayCache(cache,factory);
	}

	public static ByteArrayCache createSimple(int bufferSize, int maxEntries) {
		ObjectFactory<byte[]> factory = createFactory(bufferSize);
		ObjectCache<byte[]> cache = new LinkedObjectCache<byte[]>(factory, maxEntries);
		return new ByteArrayCache(cache,factory);
	}

	public static ByteArrayCache createScalable(int bufferSize, int maxEntries) {
		ObjectFactory<byte[]> factory = createFactory(bufferSize);
		ObjectCache<byte[]> cache = new ScalableObjectCache<byte[]>(factory, maxEntries);
		return new ByteArrayCache(cache,factory);
	}

	public static ByteArrayCache createScalable(int bufferSize, int maxEntries, int bufferBuckets) {
		ObjectFactory<byte[]> factory = createFactory(bufferSize);
		ObjectCache<byte[]> cache = new ScalableObjectCache<byte[]>(factory, maxEntries, bufferBuckets);
		return new ByteArrayCache(cache,factory);
	}

	public static ByteArrayCache createNoop(int bufferSize) {
		ObjectFactory<byte[]> factory = createFactory(bufferSize);
		ObjectCache<byte[]> cache = new NoopObjectCache<byte[]>(factory);
		return new ByteArrayCache(cache,factory);
	}

	public static ObjectFactory<byte[]> createFactory(int bufferSize) {
		return new ByteArrayFactory(bufferSize,factory);
	}
	
	@Override
	public void fillZero(byte[] e) {
		factory.fillZero(e);
	}
	
	private static final class ByteArrayFactory implements ObjectFactory<byte[]> {

		private final int bufferSize;

		public ByteArrayFactory(int bufSize) {
			this.bufferSize = bufSize;
		}

		@Override
		public void destroy(byte[] e) { /* the GC takes care of cleaning up */}

		@Override
		public byte[] create() {
			return new byte[bufferSize];
		}

		@Override
		public boolean release(byte[] e) {
			if (e.length == bufferSize) {
				Arrays.fill(e, (byte) 0);
				return true;
			}
			return false;
		}

		@Override
		public boolean canReuse(byte[] e) {
			// this is a buffer, not a connection that might have timed out, so it can always be reused
			return true;
		}
	}
}
