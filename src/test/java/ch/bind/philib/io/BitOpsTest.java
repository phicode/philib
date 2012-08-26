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
import static ch.bind.philib.io.BitOps.rotl32;
import static ch.bind.philib.io.BitOps.rotr32;
import static org.testng.Assert.assertEquals;

import java.util.Random;

import org.testng.annotations.Test;

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

	@Test
	public void rotl_32() {
		final int val = 0xFABC1234;
		assertEquals(0xFABC1234, rotl32(val, 0));
		assertEquals(0xABC1234F, rotl32(val, 4));
		assertEquals(0xBC1234FA, rotl32(val, 8));
		assertEquals(0xC1234FAB, rotl32(val, 12));
		assertEquals(0x1234FABC, rotl32(val, 16));
		assertEquals(0x234FABC1, rotl32(val, 20));
		assertEquals(0x34FABC12, rotl32(val, 24));
		assertEquals(0x4FABC123, rotl32(val, 28));
		assertEquals(0xFABC1234, rotl32(val, 32));

		// 1010b -> 0xA
		// 0101b -> 0x5
		assertEquals(0xAAAAAAAA, rotl32(0x55555555, 1));
		assertEquals(0x55555555, rotl32(0xAAAAAAAA, 1));
	}

	@Test
	public void rotr_32() {
		final int val = 0xFABC1234;
		assertEquals(0xFABC1234, rotr32(val, 0));
		assertEquals(0x4FABC123, rotr32(val, 4));
		assertEquals(0x34FABC12, rotr32(val, 8));
		assertEquals(0x234FABC1, rotr32(val, 12));
		assertEquals(0x1234FABC, rotr32(val, 16));
		assertEquals(0xC1234FAB, rotr32(val, 20));
		assertEquals(0xBC1234FA, rotr32(val, 24));
		assertEquals(0xABC1234F, rotr32(val, 28));
		assertEquals(0xFABC1234, rotr32(val, 32));

		// 1010b -> 0xA
		// 0101b -> 0x5
		assertEquals(0xAAAAAAAA, rotr32(0x55555555, 1));
		assertEquals(0x55555555, rotr32(0xAAAAAAAA, 1));
	}
}
