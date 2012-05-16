package ch.bind.philib.pool;

import java.nio.ByteBuffer;

import ch.bind.philib.pool.impl.ObjectFactory;
import ch.bind.philib.pool.impl.ObjectPool;
import ch.bind.philib.pool.impl.ObjectPoolImpl;
import ch.bind.philib.pool.impl.ScalableObjectPool;

public final class ByteBufferPool extends Bar<ByteBuffer> {

	private ByteBufferPool(ObjectFactory<ByteBuffer> factory, ObjectPool<ByteBuffer> pool) {
		super(factory, pool);
	}

	public static ByteBufferPool createSimple(int bufferSize, int maxEntries) {
		ObjectPool<ByteBuffer> pool = new ObjectPoolImpl<ByteBuffer>(maxEntries);
		return create(bufferSize, pool);
	}

	public static ByteBufferPool createScalable(int bufferSize, int maxEntries) {
		ObjectPool<ByteBuffer> pool = new ScalableObjectPool<ByteBuffer>(maxEntries);
		return create(bufferSize, pool);
	}

	public static ByteBufferPool createScalable(int bufferSize, int maxEntries, int bufferBuckets) {
		ObjectPool<ByteBuffer> pool = new ScalableObjectPool<ByteBuffer>(maxEntries, bufferBuckets);
		return create(bufferSize, pool);
	}

	public static ByteBufferPool create(int bufferSize, ObjectPool<ByteBuffer> pool) {
		ObjectFactory<ByteBuffer> factory = new ByteBufferFactory(bufferSize);
		return new ByteBufferPool(factory, pool);
	}

	private static final class ByteBufferFactory implements ObjectFactory<ByteBuffer> {

		private final int bufferSize;

		ByteBufferFactory(int bufferSize) {
			this.bufferSize = bufferSize;
		}

		@Override
		public ByteBuffer create() {
			return ByteBuffer.allocateDirect(bufferSize);
		}

		@Override
		public void destroy(ByteBuffer e) {
		}

		@Override
		public void released(ByteBuffer e) {
			e.clear();
		}
		
		@Override
		public boolean canRelease(ByteBuffer e) {
			return e.capacity() == bufferSize;
		}
	}
}
