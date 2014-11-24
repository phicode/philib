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

package ch.bind.philib.pool.buffer;

import ch.bind.philib.pool.Pool;
import ch.bind.philib.pool.manager.ByteArrayManager;
import ch.bind.philib.pool.object.ConcurrentPool;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
public class ConcurrentPoolTest extends BufferPoolTestBase<byte[]> {

	private static final int NCPU = Runtime.getRuntime().availableProcessors();

	@Override
	Pool<byte[]> createPool(int bufferSize, int maxEntries) {
		ByteArrayManager manager = new ByteArrayManager(bufferSize);
		return new ConcurrentPool<>(manager, maxEntries, true, NCPU);
	}

	@Override
	byte[] createBuffer(int bufferSize) {
		return new byte[bufferSize];
	}

	@Test
	public void atLeastTwoConcurrency() {
		ByteArrayManager manager = new ByteArrayManager(1024);
		int maxEntries = 8;
		for (int i = -10; i < 2; i++) {
			ConcurrentPool<byte[]> pool = new ConcurrentPool<>(manager, maxEntries, true, 0);
			assertEquals(pool.getConcurrency(), 2);
		}
	}
}
