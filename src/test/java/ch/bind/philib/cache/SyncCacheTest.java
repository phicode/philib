package ch.bind.philib.cache;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class SyncCacheTest {

	@Test
	public void checkPassThrough() {
		Cache<Integer, Integer> cache = Mockito.mock(Cache.class);
		Cache<Integer, Integer> sync = SyncCache.wrap(cache);

		sync.add(1, 2);
		Mockito.verify(cache).add(1, 2);

		Mockito.when(cache.get(1)).thenReturn(2);
		Integer v = sync.get(1);
		assertNotNull(v);
		assertEquals(v.intValue(), 2);
		Mockito.verify(cache).get(1);

		sync.remove(99);
		Mockito.verify(cache).remove(99);

		Mockito.when(cache.capacity()).thenReturn(123);
		assertEquals(sync.capacity(), 123);
		Mockito.verify(cache).capacity();

		sync.clear();
		Mockito.verify(cache).clear();

		Mockito.verifyNoMoreInteractions(cache);
	}
}
