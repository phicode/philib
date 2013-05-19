/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.bind.philib.io;

import static ch.bind.philib.io.BitOps.findLowestSetBitIdx64;
import static org.testng.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.testng.annotations.Test;

import ch.bind.philib.TestUtil;

public class BitOpsTest {

	private static final int TEST_LOOPS = 20000;

	private static final long SPEED_LOOPS = 8000 * 1000 * 1000L;

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
		final Random rand = ThreadLocalRandom.current();
		for (int numBits = 1; numBits < 64; numBits++) {
			for (int loop = 0; loop < TEST_LOOPS; loop++) {
				long v = 0;
				int lowestBitIdx = 64;
				for (int i = 0; i < numBits; i++) {
					int setBitIdx = rand.nextInt(64); // 0 - 63
					v |= (1L << setBitIdx);
					lowestBitIdx = Math.min(lowestBitIdx, setBitIdx);
				}
				assertEquals(lowestBitIdx, findLowestSetBitIdx64(v));
			}
		}
	}

	@Test
	public void benchmark() {
		if (!TestUtil.RUN_BENCHMARKS) {
			return;
		}
		// searching for the uppermost bit is the slowest operation
		long v = 1L << 63;
		assertEquals(63, findLowestSetBitIdx64(v));
		long tStart = System.nanoTime();
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
		long tTotal = System.nanoTime() - tStart;

		TestUtil.printBenchResults(BitOps.class, "lowestSetBit", "lsb", tTotal, SPEED_LOOPS);
	}
}
