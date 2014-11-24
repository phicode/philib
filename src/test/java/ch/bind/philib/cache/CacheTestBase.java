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

import ch.bind.philib.TestUtil;
import ch.bind.philib.lang.Cloner;
import ch.bind.philib.lang.NamedSeqThreadFactory;
import ch.bind.philib.lang.ThreadUtil;
import ch.bind.philib.math.Calc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * tests which must pass on all cache implementations.
 *
 * @author philipp meinen
 */
@Test
public abstract class CacheTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(CacheTestBase.class);
	private static final int UP_DOWN_CAP_COEFF = 5;
	private static final Cloner<Integer> INTEGER_CLONER = new Cloner<Integer>() {

		@Override
		public Integer clone(Integer value) {
			assertNotNull(value);
			return new Integer(value.intValue());
		}
	};

	public static String itos(int i) {
		return Integer.toString(i);
	}

	abstract <K, V> Cache<K, V> create();

	abstract <K, V> Cache<K, V> create(int capacity);

	abstract <K, V> Cache<K, V> create(Cloner<V> valueCloner);

	abstract int getMinCapacity();

	abstract int getBucketSize();

	abstract int getDefaultCapacity();

	@Test
	public void defaultCapacityOrMore() {
		Cache<Integer, Integer> cache;

		final int def = getDefaultCapacity();

		cache = this.create();
		assertEquals(cache.capacity(), def);

		cache = this.create(def * 4);
		assertEquals(cache.capacity(), def * 4);
	}

	@Test
	public void minCapacity() {
		this.<Integer, Integer>create(getMinCapacity());
	}

	@Test(expectedExceptions = {IllegalArgumentException.class})
	public void notLessThanMinCapacity() {
		this.<Integer, Integer>create(getMinCapacity() - getBucketSize());
	}

	@Test(expectedExceptions = {IllegalArgumentException.class})
	public void notZeroCapacity() {
		this.<Integer, Integer>create(0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getNullKey() {
		Cache<String, String> cache = this.create();
		cache.get(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void setNullKey() {
		Cache<String, String> cache = this.create();
		cache.set(null, "abc");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void setNullValue() {
		Cache<String, String> cache = this.create();
		cache.set("abc", null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void removeNullKey() {
		Cache<String, String> cache = this.create();
		cache.remove(null);
	}

	@Test
	public void get() {
		Cache<String, String> cache = this.create();

		assertNull(cache.get("1"));
		cache.set("1", "one");
		assertEquals(cache.get("1"), "one");

		cache.remove("2");
		assertEquals(cache.get("1"), "one");

		cache.remove("1");
		assertNull(cache.get("1"));
	}

	// private static final int UP_DOWN_CAP_COEFF = 150;

	@Test
	public void overwrite() {
		Cache<String, String> cache = this.create();
		cache.set("1", "version 1");
		cache.set("1", "version 2");
		assertEquals(cache.get("1"), "version 2");
		cache.remove("1");
		assertNull(cache.get("1"));
	}

	@Test
	public void cloner() {
		Cache<Integer, Integer> cache = this.create(INTEGER_CLONER);
		Integer one = Integer.valueOf(1);
		cache.set(one, one);
		Integer copy = cache.get(one);
		assertNotNull(copy);
		assertEquals(one.intValue(), copy.intValue());
		// different reference
		assertNotSame(one, copy);
	}

	@Test
	public void up() {
		Cache<Integer, Integer> cache = this.create();
		int hit = 0, miss = 0;
		final int N = cache.capacity() * UP_DOWN_CAP_COEFF;
		Integer[] is = new Integer[N];
		for (int i = 0; i < N; i++) {
			is[i] = Integer.valueOf(i);
		}
		for (int i = 0; i < N; i++) {
			cache.set(is[i], is[i]);
			for (int j = 0; j <= i; j++) {
				Integer v = null;
				if ((v = cache.get(is[j])) != null) {
					assertEquals(v, is[j]);
					hit++;
				} else {
					miss++;
				}
			}
		}
		// System.out.println("N: " + N + " hit: " + hit + " miss: " + miss);
	}

	@Test
	public void down() {
		Cache<Integer, Integer> cache = this.create();
		int hit = 0, miss = 0;
		final int N = cache.capacity() * UP_DOWN_CAP_COEFF;
		Integer[] is = new Integer[N];
		for (int i = 0; i < N; i++) {
			is[i] = Integer.valueOf(i);
		}
		for (int i = 0; i < N; i++) {
			cache.set(is[i], is[i]);
			for (int j = i; j >= 0; j--) {
				Integer v = null;
				if ((v = cache.get(is[j])) != null) {
					assertEquals(v, is[j]);
					hit++;
				} else {
					miss++;
				}
			}
		}
		// System.out.println("N: " + N + " hit: " + hit + " miss: " + miss);
	}

	@Test
	public void softReferences() {
		byte[] data = new byte[512 * 1024]; // 512KiB
		new SecureRandom().nextBytes(data);

		// Make the cache hold on to more data than it possibly can
		// 16GiB if the vm has no specified upper limit
		long vmmax = Runtime.getRuntime().maxMemory();
		vmmax = vmmax == Long.MAX_VALUE ? 16L * 1024 * 1024 * 1024 : (long) (vmmax * 1.05);

		int bs = getBucketSize();
		int cap = (int) Calc.ceilDiv(vmmax, data.length);
		if (cap % bs != 0) {
			cap += (bs - (cap % bs));
		}

		long t0 = System.nanoTime();
		Cache<Integer, byte[]> cache = this.create(cap);
		long t1 = System.nanoTime() - t0;
		for (int i = 0; i < cap; i++) {
			cache.set(i, data.clone());
		}
		long t2 = System.nanoTime() - t0 - t1;
		int inMem = 0;
		for (int i = 0; i < cap; i++) {
			if (cache.get(i) != null) {
				inMem++;
			}
		}
		// the JVM must have thrown away some of the soft-references
		assertTrue(inMem < cap, inMem + " >= " + cap);
		long t3 = System.nanoTime() - t0 - t1 - t2;

		// remove all hard-referenced to SoftReference objects and give the JVM
		// a chance to free memory
		cache.clear();
		TestUtil.gcAndSleep(100);

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("JVM held on to %d out of %d elements => %dMiB\n", inMem, cap, inMem / 2));
			LOG.debug(String.format("times[init=%.3fms, filling %.1fGiB: %.3fms, counting live entries: %.3fms]\n", //
					t1 / 1000000f, cap / 2048f, t2 / 1000000f, t3 / 1000000f));
		}
	}

	@Test
	public void stressTest() throws InterruptedException {
		if (!TestUtil.RUN_STRESS_TESTS) {
			return;
		}
		int concurrency = Runtime.getRuntime().availableProcessors() * 64;
		int values = concurrency * 2;
		int minutes = 2;
		System.out.printf("stress testing %s for %d minutes with concurrency %d over %d values\n", //
				getClass().getSimpleName(), minutes, concurrency, values);
		StressTester[] sts = new StressTester[concurrency];
		Cache<Integer, Integer> cache = create(concurrency);
		for (int i = 0; i < concurrency; i++) {
			sts[i] = new StressTester(cache, values);
		}
		Thread[] ts = ThreadUtil.createThreads(sts, new NamedSeqThreadFactory("Cache Stress-Test"));
		ThreadUtil.startThreads(ts);
		Thread.sleep(minutes * 60 * 1000);
		ThreadUtil.interruptAndJoinThreads(ts);
		long iterations = 0;
		for (StressTester st : sts) {
			iterations += st.iterations;
			assertTrue(st.ok);
		}
		System.out.printf("finished with %d iterations\n", iterations);
	}

	private class StressTester implements Runnable {

		private final Cache<Integer, Integer> cache;
		private final Integer[] keys;

		final CountDownLatch finished = new CountDownLatch(1);
		boolean ok;
		long iterations;

		public StressTester(Cache<Integer, Integer> cache, int n) {
			this.cache = cache;
			this.keys = new Integer[n];
			for (int i = 0; i < n; i++) {
				keys[i] = i;
			}
		}

		@Override
		public void run() {
			Thread t = Thread.currentThread();
			long iterations = 0;
			try {
				while (!t.isInterrupted()) {
					for (Integer key : keys) {
						if (cache.get(key) == null) {
							cache.set(key, key);
						}
					}
					iterations++;
				}
				ok = true;
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			} finally {
				this.iterations = iterations;
				finished.countDown();
			}
		}
	}
}
