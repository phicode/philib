package ch.bind.philib.cache.lru.newimpl;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class StagedCacheEntryTest {

	@Test
	public void toggleOldGenBit() {
		StagedCacheEntry<Integer, Integer> x = new StagedCacheEntry<Integer, Integer>(1, 2);
		assertTrue(x.isInLruYoungGen());
		x.setInLruYoungGen(true);
		assertTrue(x.isInLruYoungGen());

		x.setInLruYoungGen(false);
		assertFalse(x.isInLruYoungGen());
		x.setInLruYoungGen(false);
		assertFalse(x.isInLruYoungGen());

		x.setInLruYoungGen(true);
		assertTrue(x.isInLruYoungGen());

		assertEquals(x.recordHit(), 1);
		assertTrue(x.isInLruYoungGen());

		x.setInLruYoungGen(false);
		assertFalse(x.isInLruYoungGen());
		
		assertEquals(x.recordHit(), 2);
		assertFalse(x.isInLruYoungGen());
	}
}
