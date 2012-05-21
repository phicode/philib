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

		ByteBufferFactory(int bufferSize) {
			this.bufferSize = bufferSize;
		}

		@Override
		public ByteBuffer create() {
			 return ByteBuffer.allocateDirect(bufferSize);
//			return ByteBuffer.allocate(bufferSize);
		}

		@Override
		public void destroy(ByteBuffer e) {
		}

		@Override
		public boolean release(ByteBuffer e) {
			if (e.capacity() == bufferSize) {
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
