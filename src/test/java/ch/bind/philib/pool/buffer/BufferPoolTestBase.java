package ch.bind.philib.pool.buffer;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import ch.bind.philib.pool.Pool;

public abstract class BufferPoolTestBase<T> {

	abstract Pool<T> createPool(int bufferSize, int maxEntries);

	abstract T createBuffer(int bufferSize);

	@Test
	public void testMaxEntries() {
		int maxEntries = 1;
		int bufferSize = 8192;
		Pool<T> pool = createPool(bufferSize, maxEntries);

		// create 2 elements
		T in1 = createBuffer(bufferSize);
		T in2 = createBuffer(bufferSize);

		// the cache must accept this one because it is empty
		pool.recycle(in1);
		// but it must ignore this entry because it is full
		pool.recycle(in2);

		T out1 = pool.take();
		T out2 = pool.take();

		// out1 should be in1
		// out2 should have been newly created
		assertTrue(out1 == in1);
		assertTrue(out2 != in2);
	}

	@Test
	public void useAnyBuffers() {
		int maxEntries = 10;
		int bufferSize = 8192;
		Pool<T> pool = createPool(bufferSize, maxEntries);
		T in = createBuffer(bufferSize);

		pool.recycle(in);

		T out = pool.take();
		T outNew = pool.take();

		assertTrue(in == out);
		assertTrue(in != outNew);
	}

	@Test
	public void onlyTakeCorrectSizeBuffers() {
		int maxEntries = 10;
		int bufferSize = 8192;
		Pool<T> pool = createPool(bufferSize, maxEntries);
		T b = createBuffer(bufferSize / 2);

		pool.recycle(b);

		T b2 = pool.take();

		assertTrue(b != b2);
	}
}
