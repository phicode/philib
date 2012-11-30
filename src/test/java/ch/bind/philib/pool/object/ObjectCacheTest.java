package ch.bind.philib.pool.object;

import org.testng.annotations.Test;

import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.manager.ObjectManager;

import static org.testng.Assert.*;

public class ObjectCacheTest {

	@Test
	public void halfRecycleable() {
		Pool<Integer> pool = new StrongPool<Integer>(new RecycleOddManager(), 8);
		for (int i = 0; i < 10; i++) {
			assertEquals(pool.take().intValue(), i);
		}
		for (int i = 0; i < 10; i++) {
			pool.recycle(Integer.valueOf(i));
		}
		for (int i = 1; i < 10; i += 2) {
			assertEquals(pool.take().intValue(), i);
		}
		assertEquals(pool.take().intValue(), 10);
	}

	private static final class RecycleOddManager implements ObjectManager<Integer> {

		private int next;

		@Override
		public Integer create() {
			return Integer.valueOf(next++);
		}

		@Override
		public void release(Integer value) {
		}

		@Override
		public boolean prepareForRecycle(Integer value) {
			return value.intValue() % 2 == 1;
		}

		@Override
		public boolean canReuse(Integer value) {
			return true;
		}
	}
}
