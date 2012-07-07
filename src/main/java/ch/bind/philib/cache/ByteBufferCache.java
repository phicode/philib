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

import java.nio.ByteBuffer;

import ch.bind.philib.cache.impl.LinkedObjectCache;
import ch.bind.philib.cache.impl.ObjectFactory;
import ch.bind.philib.cache.impl.ScalableObjectCache;

public final class ByteBufferCache extends SpecificObjectCache<ByteBuffer> {

	private ByteBufferCache(ObjectCache<ByteBuffer> cache) {
		super(cache);
	}

	public static ByteBufferCache createSimple(int bufferSize, int maxEntries) {
		ObjectFactory<ByteBuffer> factory = createFactory(bufferSize);
		ObjectCache<ByteBuffer> cache = new LinkedObjectCache<ByteBuffer>(factory, maxEntries);
		return new ByteBufferCache(cache);
	}

	public static ByteBufferCache createScalable(int bufferSize, int maxEntries) {
		ObjectFactory<ByteBuffer> factory = createFactory(bufferSize);
		ObjectCache<ByteBuffer> cache = new ScalableObjectCache<ByteBuffer>(factory, maxEntries);
		return new ByteBufferCache(cache);
	}

	public static ByteBufferCache createScalable(int bufferSize, int maxEntries, int bufferBuckets) {
		ObjectFactory<ByteBuffer> factory = createFactory(bufferSize);
		ObjectCache<ByteBuffer> cache = new ScalableObjectCache<ByteBuffer>(factory, maxEntries, bufferBuckets);
		return new ByteBufferCache(cache);
	}

	public static ObjectFactory<ByteBuffer> createFactory(int bufferSize) {
		return new ByteBufferFactory(bufferSize);
	}

	private static final class ByteBufferFactory implements ObjectFactory<ByteBuffer> {

		private final int bufferSize;

		private final byte[] nullFiller;

		ByteBufferFactory(int bufferSize) {
			this.bufferSize = bufferSize;
			this.nullFiller = new byte[bufferSize];
		}

		@Override
		public ByteBuffer create() {
			return ByteBuffer.allocateDirect(bufferSize);
			// return ByteBuffer.allocate(bufferSize);
		}

		@Override
		public void destroy(ByteBuffer e) {
		}

		@Override
		public boolean release(ByteBuffer e) {
			if (e.capacity() == bufferSize) {
				e.clear();
				e.put(nullFiller);
				e.clear();
				return true;
			}
			return false;
		}

		@Override
		public boolean canReuse(ByteBuffer e) {
			return true;
		}
	}
}
