package ch.bind.philib.pool.buffer;

import org.testng.annotations.Test;

import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.manager.ByteArrayManager;
import ch.bind.philib.pool.object.ConcurrentPool;

@Test
public class ConcurrentTest extends BufferPoolTestBase<byte[]> {

	@Override
	Pool<byte[]> createPool(int bufferSize, int maxEntries) {
		ByteArrayManager manager = new ByteArrayManager(bufferSize);
		return new ConcurrentPool<byte[]>(manager, maxEntries, true, 4);
	}

	@Override
	byte[] createBuffer(int bufferSize) {
		return new byte[bufferSize];
	}
}
