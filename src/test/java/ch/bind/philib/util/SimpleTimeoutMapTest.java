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

package ch.bind.philib.util;

import ch.bind.philib.TestUtil;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class SimpleTimeoutMapTest {

	@Test
	public void putEntry() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		map.put(0, 123, 456);
		assertEquals(1, map.size());
		assertFalse(map.isEmpty());
		map.remove(123);
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	public void duplicateKeys() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		int key = 12345678;
		Integer prev1 = map.put(1000, key, 1);
		Integer prev2 = map.put(1000, key, 1234);
		assertNull(prev1);
		assertTrue(prev2 != null && prev2 == 1);
		assertEquals(map.get(key).intValue(), 1234);
		long timeToNextTimeout = map.getTimeToNextTimeout();
		assertTrue(timeToNextTimeout > 800 && timeToNextTimeout <= 1000);
	}

	@Test
	public void nullOnEmpty() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		assertTrue(map.isEmpty());
		assertNull(map.pollTimeoutNow());
		assertNull(map.get(1));
	}

	@Test
	public void returnNullOnNoTimeouts() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		map.put(1, 123, 456);
		assertFalse(map.isEmpty());
		assertNull(map.pollTimeoutNow());
	}

	@Test
	public void returnZeroOnImmediateTimeout() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		map.put(0, 123, 456);
		assertEquals(map.getTimeToNextTimeout(), 0);
		assertNotNull(map.pollTimeoutNow());
		assertTrue(map.isEmpty());
	}

	@Test
	public void longMaxValueOnEmpty() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		assertEquals(map.getTimeToNextTimeout(), Long.MAX_VALUE);
	}

	@Test
	public void removeOnEmptyReturnsNull() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		map.put(1000, 123, 456);
		assertEquals(map.remove(123).intValue(), 456);
		assertNull(map.remove(123));
	}

	@Test
	public void clear() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		map.put(1, 123, 456);
		map.clear();
		assertEquals(map.size(), 0);
		assertTrue(map.isEmpty());
		assertFalse(map.containsKey(123));
		assertNull(map.remove(123));
	}

	@Test
	public void manyEntries() {
		final int N = 100000;
		// prepare integers
		Integer[] ints = new Integer[N];
		int[] keys = new int[N];
		for (int i = 0; i < N; i++) {
			ints[i] = i;
		}
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		for (int i = 0; i < N; i++) {
			map.put(0, ints[i], ints[i]);
		}
		for (int i = 0; i < N; i++) {
			Entry<Integer, Integer> e = map.pollTimeoutNow();
			assertNotNull(e);
			assertEquals(e.getKey(), e.getValue());
			keys[i] = e.getKey();
		}
		assertTrue(map.isEmpty());
		// the first key should be on the exact timestamp, the others
		// maybe distributed randomly, but all in there
		assertEquals(keys[0], 0);
		Arrays.sort(keys);

		for (int i = 1; i < N; i++) {
			assertEquals(keys[i], i);
		}
	}

	@Test
	public void entryEqualsAndHashcode() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();

		map.put(0, 111, 222);
		Entry<Integer, Integer> a1 = map.pollTimeoutNow();

		map.put(0, 111, 222);
		Entry<Integer, Integer> a2 = map.pollTimeoutNow();

		map.put(0, 111, 111);
		Entry<Integer, Integer> b = map.pollTimeoutNow();

		map.put(0, 222, 222);
		Entry<Integer, Integer> c = map.pollTimeoutNow();

		assertNotNull(a1);
		assertNotNull(a2);
		assertNotNull(b);
		assertNotNull(c);

		assertEquals(a1.hashCode(), a2.hashCode());
		assertTrue(a1.equals(a1));
		assertTrue(a1.equals(a2));
		assertFalse(a1.equals(b));
		assertFalse(a1.equals(c));
		assertFalse(a1.equals("abc"));
	}

	@Test
	public void accuratePollTimeoutNow() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		Integer kv = 1;
		long totalNs = 0;

		for (int i = 0; i < 10; i++) {
			long start = System.nanoTime();
			map.put(100, kv, kv);
			while (map.pollTimeoutNow() == null) {
				Thread.yield();
			}
			long time = System.nanoTime() - start;
			totalNs += time;
		}
		assertTrue(totalNs >= TimeUnit.MILLISECONDS.toNanos(1000) && totalNs <= TimeUnit.MILLISECONDS.toNanos(1100), "totalNs: " + totalNs);
	}

	@Test
	public void accuratePollTimeout() throws InterruptedException {
		TestUtil.gcAndSleep(50);
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		Integer kv = 1;
		long totalNs = 0;

		for (int i = 0; i < 10; i++) {
			long start = System.nanoTime();
			map.put(100, kv, kv);
			assertNotNull(map.pollTimeoutBlocking(500, TimeUnit.MILLISECONDS));
			long time = System.nanoTime() - start;
			totalNs += time;
		}
		assertTrue(totalNs >= TimeUnit.MILLISECONDS.toNanos(1000) && totalNs <= TimeUnit.MILLISECONDS.toNanos(1100), "totalNs: " + totalNs);
	}

	@Test
	public void accuratePollTimeout2() throws InterruptedException {
		TestUtil.gcAndSleep(50);
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		Integer kv = 1;
		long totalNs = 0;

		for (int i = 0; i < 10; i++) {
			long start = System.nanoTime();
			map.put(100, kv, kv);
			assertNotNull(map.pollTimeoutBlocking());
			long time = System.nanoTime() - start;
			totalNs += time;
		}
		assertTrue(totalNs >= TimeUnit.MILLISECONDS.toNanos(1000) && totalNs <= TimeUnit.MILLISECONDS.toNanos(1100), "totalNs: " + totalNs);
	}

	@Test
	public void accuratePollTimeout3() throws InterruptedException {
		TestUtil.gcAndSleep(50);
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		Integer kv = 1;
		long totalT1 = 0;
		long totalT2 = 0;

		for (int i = 0; i < 10; i++) {
			long start = System.nanoTime();
			map.put(100, kv, kv);
			assertNull(map.pollTimeoutBlocking(50, TimeUnit.MILLISECONDS));
			long t1 = System.nanoTime() - start;
			assertNotNull(map.pollTimeoutBlocking());
			long t2 = System.nanoTime() - start - t1;
			totalT1 += t1;
			totalT2 += t2;
		}
		assertTrue(totalT1 >= TimeUnit.MILLISECONDS.toNanos(490) && totalT1 <= TimeUnit.MILLISECONDS.toNanos(550), "totalT1: " + totalT1);
		assertTrue(totalT2 >= TimeUnit.MILLISECONDS.toNanos(490) && totalT2 <= TimeUnit.MILLISECONDS.toNanos(550), "totalT2: " + totalT2);
	}

	@Test
	public void blockingPool() throws InterruptedException {
		final TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		final AtomicBoolean ok = new AtomicBoolean(false);
		final CountDownLatch started = new CountDownLatch(1);
		final CountDownLatch finished = new CountDownLatch(1);
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					started.countDown();
					Entry<Integer, Integer> e = map.pollTimeoutBlocking();
					if (e != null) {
						ok.set(true);
					}
				} catch (InterruptedException e) {
					fail(e.getMessage());
				}
				finished.countDown();
			}
		});
		t.setDaemon(true);
		t.start();

		assertTrue(started.await(50, TimeUnit.MILLISECONDS));
		// wait some more so that the thread can enter the poll method and wait
		// on the condition
		Thread.sleep(5);

		map.put(100, 1, 1);

		assertTrue(finished.await(150, TimeUnit.MILLISECONDS));
		assertTrue(ok.get());
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void entrySetValueUnsupported() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		map.put(0, 123, 456);
		Entry<Integer, Integer> e = map.pollTimeoutNow();
		assertNotNull(e);
		e.setValue(123);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "timeout must not be negative")
	public void addNoNegativeTimeout() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		map.put(-1, 1, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "key must not be null")
	public void noNullKey() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		map.put(0, null, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "value must not be null")
	public void noNullValue() {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		map.put(0, 1, null);
	}

	@Test
	public void pollAllTimeoutNow() throws InterruptedException {
		TimeoutMap<Integer, Integer> map = new SimpleTimeoutMap<>();
		List<Entry<Integer, Integer>> tos = map.pollAllTimeoutNow();
		assertNotNull(tos);
		assertTrue(tos.isEmpty());

		// timeout in 15sec
		map.put(15000L, 1, 1);
		tos = map.pollAllTimeoutNow();
		assertNotNull(tos);
		assertTrue(tos.isEmpty());

		// 1 timeout immediately
		map.put(1L, 2, 2);
		Thread.sleep(5);
		tos = map.pollAllTimeoutNow();
		assertNotNull(tos);
		assertEquals(tos.size(), 1);
		assertEquals(tos.get(0).getKey().intValue(), 2);

		// 2 timeouts immediately
		map.put(1L, 3, 3);
		map.put(10L, 4, 4);
		Thread.sleep(15);
		tos = map.pollAllTimeoutNow();
		assertNotNull(tos);
		assertEquals(tos.size(), 2);
		assertEquals(tos.get(0).getKey().intValue(), 3);
		assertEquals(tos.get(1).getKey().intValue(), 4);
	}
}
