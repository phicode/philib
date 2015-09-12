/*
 * Copyright (c) 2015 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.buf;

import ch.bind.philib.lang.ArrayUtil;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class HashDedupBufferTest {

	@Test
	public void allUnique() {
		final int N = 100000;
		DedupBuffer dedup = new HashDedupBuffer(8192);
		byte[] payload = new byte[16];
		for (int i = 0; i < N; i++) {
			increment(payload);
			assertTrue(dedup.add(payload));
		}
		// System.out.println("final size:" + dedup.size());
	}

	// increment data and overflow to next byte on hex boundary
	private static void increment(byte[] data) {
		for (int i = data.length - 1; i >= 0; i--) {
			data[i]++;
			if (data[i] < 16) {
				return;
			}
			// overflow => continue with next byte
			data[i] = 0;
		}
	}

	@Test
	public void duplicate() {
		final int N = 1024;
		DedupBuffer dedup = new HashDedupBuffer(8192);
		byte[] payload = new byte[16];
		for (int i = 0; i < N; i++) {
			increment(payload);
			assertTrue(dedup.add(payload));
		}
		// reset payload
		ArrayUtil.memclr(payload);

		// verify that all messages are still in the dedup buffer
		for (int i = 0; i < N; i++) {
			increment(payload);
			assertFalse(dedup.add(payload));
		}
		// System.out.println("final size:" + dedup.size());
	}

	@Test
	public void sizeAndNumBucketsOne() {
		DedupBuffer dedup = new HashDedupBuffer(1, 1, HashDedupBuffer.DEFAULT_DIGEST_ALGORITHM);
		assertTrue(dedup.add("first".getBytes()));
		assertFalse(dedup.add("first".getBytes()));
		assertTrue(dedup.add("second".getBytes()));
		assertFalse(dedup.add("second".getBytes()));
		assertTrue(dedup.add("first".getBytes()));
		assertFalse(dedup.add("first".getBytes()));
	}
}
