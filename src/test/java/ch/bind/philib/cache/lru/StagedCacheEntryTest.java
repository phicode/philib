package ch.bind.philib.cache.lru;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class StagedCacheEntryTest {

	@Test
	public void toggleOldGenBit() {
		StagedCacheEntry<Integer, Integer> x = new StagedCacheEntry<Integer, Integer>(1, 2);
		assertTrue(x.isInYoungGen());
		x.setInYoungGen();
		assertTrue(x.isInYoungGen());

		x.setInOldGen();
		assertFalse(x.isInYoungGen());
		x.setInOldGen();
		assertFalse(x.isInYoungGen());

		x.setInYoungGen();
		assertTrue(x.isInYoungGen());

		assertEquals(x.recordHit(), 1);
		assertTrue(x.isInYoungGen());

		x.setInOldGen();
		assertFalse(x.isInYoungGen());

		assertEquals(x.recordHit(), 2);
		assertFalse(x.isInYoungGen());
	}
}
