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

package ch.bind.philib.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import ch.bind.philib.TestUtil;
import ch.bind.philib.math.Calc;

/**
 * tests which must pass on all cache implementations.
 * 
 * @author philipp meinen
 * 
 */
@Test(singleThreaded = true)
public abstract class CacheTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(CacheTestBase.class);

	abstract <K, V> Cache<K, V> create();

	abstract <K, V> Cache<K, V> create(int capacity);

	abstract <K, V> Cache<K, V> create(int capacity, Cloner<V> valueCloner);

	abstract int getMinCapacity();

	abstract int getDefaultCapacity();

	@Test
	public void capacity() {
		Cache<Integer, Integer> cache;

		final int min = getMinCapacity();
		final int def = getDefaultCapacity();

		cache = this.<Integer, Integer> create();
		assertEquals(cache.capacity(), def);

		cache = this.<Integer, Integer> create(def * 4);
		assertEquals(cache.capacity(), def * 4);

		cache = this.<Integer, Integer> create(min - 1);
		assertEquals(cache.capacity(), min);

		cache = this.<Integer, Integer> create(0);
		assertEquals(cache.capacity(), min);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getNullKey() {
		Cache<String, String> cache = this.<String, String> create();
		cache.get(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void addNullKey() {
		Cache<String, String> cache = this.<String, String> create();
		cache.add(null, "abc");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void removeNullKey() {
		Cache<String, String> cache = this.<String, String> create();
		cache.remove(null);
	}

	@Test
	public void get() {
		Cache<String, String> cache = this.<String, String> create();

		assertNull(cache.get("1"));
		cache.add("1", "one");
		assertEquals(cache.get("1"), "one");

		cache.remove("2");
		assertEquals(cache.get("1"), "one");

		cache.remove("1");
		assertNull(cache.get("1"));
	}

	@Test
	public void overwrite() {
		Cache<String, String> cache = this.<String, String> create();
		cache.add("1", "version 1");
		cache.add("1", "version 2");
		assertEquals(cache.get("1"), "version 2");
		cache.add("1", null);
		assertNull(cache.get("1"));
		// overwrite again for full branch-coverage
		cache.add("1", null);
		assertNull(cache.get("1"));
	}

	@Test
	public void cloner() {
		Cache<Integer, Integer> cache = this.<Integer, Integer> create(10, INTEGER_CLONER);
		Integer one = Integer.valueOf(1);
		cache.add(one, one);
		Integer copy = cache.get(one);
		assertNotNull(copy);
		assertEquals(one.intValue(), copy.intValue());
		// different reference
		assertTrue(one != copy);
	}

	private static final Cloner<Integer> INTEGER_CLONER = new Cloner<Integer>() {
		@Override
		public Integer clone(Integer value) {
			assertNotNull(value);
			return new Integer(value.intValue());
		}
	};

	@Test
	public void softReferences() {
		byte[] data = new byte[512 * 1024]; // 512KiB
		new SecureRandom().nextBytes(data);

		// Make the cache hold on to more data than it possibly can
		// 16GiB if the vm has no specified upper limit
		long vmmax = Runtime.getRuntime().maxMemory();
		vmmax = vmmax == Long.MAX_VALUE ? 16L * 1024 * 1024 * 1024 : (long) (vmmax * 1.05);

		final int cap = (int) Calc.ceilDiv(vmmax, data.length);
		long t0 = System.nanoTime();
		Cache<Integer, byte[]> cache = this.<Integer, byte[]> create(cap);
		long t1 = System.nanoTime() - t0;
		for (int i = 0; i < cap; i++) {
			cache.add(i, data.clone());
		}
		long t2 = System.nanoTime() - t0 - t1;
		int inMem = 0;
		for (int i = 0; i < cap; i++) {
			if (cache.get(i) != null) {
				inMem++;
			}
		}
		// the JVM must have thrown away some of the soft-references
		assertTrue(inMem < cap);
		long t3 = System.nanoTime() - t0 - t1 - t2;

		// remove all hard-referenced to SoftReference objects and give the JVM
		// a chance to free memory
		cache.clear();
		TestUtil.gcAndSleep(100);

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("JVM held on to %d out of %d added elements => %dMiB\n", inMem, cap, inMem / 2));
			LOG.debug(String.format("times[init=%.3fms, filling %.1fGiB: %.3fms, counting live entries: %.3fms]\n", //
					t1 / 1000000f, cap / 2048f, t2 / 1000000f, t3 / 1000000f));
		}
	}

	public static String itos(int i) {
		return Integer.toString(i);
	}
}
