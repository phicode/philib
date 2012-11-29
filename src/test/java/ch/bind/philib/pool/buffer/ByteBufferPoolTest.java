package ch.bind.philib.pool.buffer;

import java.nio.ByteBuffer;

import org.testng.annotations.Test;

import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.manager.ByteBufferManager;

@Test
public class ByteBufferPoolTest extends BufferPoolTestBase<ByteBuffer> {

	@Override
	Pool<ByteBuffer> createPool(int bufferSize, int maxEntries) {
		return ByteBufferPool.create(bufferSize, maxEntries);
	}

	@Override
	ByteBuffer createBuffer(int bufferSize) {
		if (ByteBufferManager.DEFAULT_DIRECT_BUFFER) {
			return ByteBuffer.allocateDirect(bufferSize);
		}
		return ByteBuffer.allocate(bufferSize);
	}
}
