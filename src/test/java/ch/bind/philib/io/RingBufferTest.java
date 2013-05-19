/*
 * Copyright (c) 2006-2011 Philipp Meinen <philipp@bind.ch>
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.testng.annotations.Test;

import ch.bind.philib.TestUtil;
import ch.bind.philib.util.TLR;

public class RingBufferTest {

	private static final int MB = 1024 * 1024;

	private static final long GB = 1024 * MB;

	private static final int TEST_BUF_SIZE = 1 * MB;

	private static final int RANDOM_TEST_SIZE = 16 * MB;

	private static final int RANDOM_TEST_MAX_BUF_SIZE = 4 * MB;

	private static final int RANDOM_TEST_MAX_CHUNK_SIZE = 256;

	private static final long PERF_SIZE = 32 * GB;

	private static final int PERF_CHUNKSIZE = 4096;

	private static final int REPEATS = 4;

	@Test
	public void frontAndBack() {
		LinkedList<byte[]> bufExp = new LinkedList<byte[]>();
		RingBuffer buf = new RingBuffer();
		assertEquals(0, buf.available());
		final int chunkSize = 16;
		int chunkIdx = 0;
		int size = 0;
		while (size < TEST_BUF_SIZE) {
			byte[] d = genData(chunkSize);
			if (chunkIdx % 2 == 0) {
				buf.write(d);
				bufExp.addLast(d);
			} else {
				buf.writeFront(d);
				bufExp.addFirst(d);
			}
			size += chunkSize;
			assertEquals(size, buf.available());
			chunkIdx++;
		}
		verifyBuf(bufExp, buf);
	}

	@Test
	public void randomAccess() {
		final Random rand = TLR.current();
		final LinkedList<Byte> bufExp = new LinkedList<Byte>();
		final RingBuffer ringBuf = new RingBuffer();
		int size = 0;
		long performed = 0;
		while (performed < RANDOM_TEST_SIZE) {
			int len = rand.nextInt(RANDOM_TEST_MAX_CHUNK_SIZE) + 1;
			byte[] buf = new byte[len];
			int a = ringBuf.available();
			boolean doRead = a >= len ? rand.nextBoolean() : false;
			if (!doRead && a + len > RANDOM_TEST_MAX_BUF_SIZE) {
				doRead = true;
			}
			boolean doFront = rand.nextBoolean();
			if (doRead) {
				if (doFront) {
					ringBuf.read(buf);
					verifyRead(buf, bufExp);
				} else {
					ringBuf.readBack(buf);
					verifyReadBack(buf, bufExp);
				}
				size -= len;
			} else {
				rand.nextBytes(buf);
				if (doFront) {
					ringBuf.writeFront(buf);
					prepend(buf, bufExp);
				} else {
					ringBuf.write(buf);
					append(buf, bufExp);
				}
				size += len;
			}
			assertEquals(size, ringBuf.available());
			assertEquals(size, bufExp.size());
			performed += len;
		}
	}

	@Test
	public void benchmark() {
		if (!TestUtil.RUN_BENCHMARKS) {
			return;
		}
		for (int i = 0; i < REPEATS; i++) {
			doBenchmark();
		}
	}

	private void doBenchmark() {
		final long start = System.nanoTime();
		RingBuffer ringBuf = new RingBuffer();
		byte[] buf = new byte[PERF_CHUNKSIZE];
		TLR.current().nextBytes(buf);
		long performed = 0;
		while (performed < PERF_SIZE) {
			ringBuf.write(buf);
			ringBuf.writeFront(buf);
			ringBuf.read(buf);
			ringBuf.writeFront(buf);
			ringBuf.readBack(buf);
			ringBuf.write(buf);
			ringBuf.read(buf);
			ringBuf.writeFront(buf);
			ringBuf.readBack(buf);
			ringBuf.read(buf);
			performed += (PERF_CHUNKSIZE * 10);
			assertEquals(0, ringBuf.available());
		}
		final long timeNs = System.nanoTime() - start;
		double mib = ((double) performed / MB);
		TestUtil.printBenchResults(RingBuffer.class, "MiB", "MiB", timeNs, mib);
	}

	private static void verifyReadBack(byte[] bs, LinkedList<Byte> bufExp) {
		for (int i = bs.length - 1; i >= 0; i--) {
			byte b = bs[i];
			byte e = bufExp.removeLast();
			assertEquals(e, b);
		}
	}

	private static void verifyRead(byte[] bs, LinkedList<Byte> bufExp) {
		for (byte b : bs) {
			byte e = bufExp.removeFirst();
			assertEquals(e, b);
		}
	}

	private static void prepend(byte[] bs, LinkedList<Byte> bufExp) {
		for (int i = bs.length - 1; i >= 0; i--) {
			bufExp.addFirst(bs[i]);
		}
	}

	private static void append(byte[] bs, LinkedList<Byte> bufExp) {
		for (byte b : bs) {
			bufExp.add(b);
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void notNullRead() {
		RingBuffer buf = new RingBuffer();
		buf.read(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void notNullReadOffLen() {
		RingBuffer buf = new RingBuffer();
		buf.read(null, 0, 0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void notNegativeOffsetRead() {
		RingBuffer buf = new RingBuffer();
		byte[] b = new byte[16];
		buf.write(b);
		buf.read(b, -1, 16);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void notNegativeLengthRead() {
		RingBuffer buf = new RingBuffer();
		byte[] b = new byte[16];
		buf.write(b);
		buf.read(b, 0, -1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void tooBigReadForInbuf() {
		RingBuffer buf = new RingBuffer();
		byte[] b = new byte[16];
		buf.write(b);
		buf.write(b);
		assertEquals(32, buf.available());
		buf.read(b, 0, 17); // too big read
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void tooBigWriteForOutbuf() {
		RingBuffer buf = new RingBuffer();
		byte[] b = new byte[16];
		buf.write(b, 0, 17); // too big write
	}

	@Test
	public void tooBigReadForBuf() {
		RingBuffer buf = new RingBuffer();
		byte[] b = new byte[16];
		buf.write(b, 0, 8); // 8 bytes in buffer
		try {
			buf.read(b, 0, 9); // try to read too much
			fail("should have thrown an illegal-argument-exc");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void clear() {
		RingBuffer buf = new RingBuffer();
		byte[] a = { 0, 1, 2, 3 };
		buf.write(a);
		assertEquals(a.length, buf.available());
		buf.clear();
		assertEquals(0, buf.available());
	}

	private static void verifyBuf(List<byte[]> expected, RingBuffer buf) {
		byte[] a = null;
		int expSize = buf.available();
		for (byte[] e : expected) {
			if (a == null || a.length != e.length) {
				a = new byte[e.length];
			}
			assertEquals(expSize, buf.available());
			buf.read(a);
			expSize -= a.length;
			assertTrue(Arrays.equals(e, a));
			assertEquals(expSize, buf.available());
		}
		assertEquals(0, buf.available());
	}

	private byte[] genData(int num) {
		byte[] d = new byte[num];
		TLR.current().nextBytes(d);
		return d;
	}
}
