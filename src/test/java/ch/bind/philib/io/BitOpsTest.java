package ch.bind.philib.io;

import static ch.bind.philib.io.BitOps.findLowestSetBitIdx64;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

public class BitOpsTest {

	private static final int TEST_LOOPS = 20000;

	private static final long SPEED_LOOPS = 2000000000L;

	@Test
	public void oneBit() {
		assertEquals(-1, findLowestSetBitIdx64(0));
		for (int idx = 0; idx < 64; idx++) {
			long v = 1L << idx;
			assertEquals(idx, findLowestSetBitIdx64(v));
		}
	}

	@Test
	public void randomBits() {
		Random r = new Random();
		for (int numBits = 1; numBits < 64; numBits++) {
			for (int loop = 0; loop < TEST_LOOPS; loop++) {
				long v = 0;
				int lowestBitIdx = 64;
				for (int i = 0; i < numBits; i++) {
					int setBitIdx = r.nextInt(64); // 0 - 63
					v |= (1L << setBitIdx);
					lowestBitIdx = Math.min(lowestBitIdx, setBitIdx);
				}
				assertEquals(lowestBitIdx, findLowestSetBitIdx64(v));
			}
		}
	}

	@Test
	public void speedTest() {
		// searching for the uppermost bit is the slowest operation
		long v = 1L << 63;
		assertEquals(63, findLowestSetBitIdx64(v));
		long tStart = System.currentTimeMillis();
		long total = 0;
		long expected = 0;
		for (long i = 0; i < SPEED_LOOPS; i += 10) {
			total += findLowestSetBitIdx64(v);
			total += findLowestSetBitIdx64(v);
			total += findLowestSetBitIdx64(v);
			total += findLowestSetBitIdx64(v);
			total += findLowestSetBitIdx64(v);
			total += findLowestSetBitIdx64(v);
			total += findLowestSetBitIdx64(v);
			total += findLowestSetBitIdx64(v);
			total += findLowestSetBitIdx64(v);
			total += findLowestSetBitIdx64(v);
			expected += (63 * 10);
		}
		assertEquals(expected, total);
		long tEnd = System.currentTimeMillis();
		long tTotal = tEnd - tStart;
		double perMsec = SPEED_LOOPS / ((double) tTotal);
		System.out.printf("%d bit ops in %dms %.1fops/ms %n", SPEED_LOOPS, tTotal, perMsec);
	}
}
