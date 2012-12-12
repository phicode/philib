/**
 * Copyright: Copyright (c) 2010-2011 Siemens IT Solutions and Services AG
 *
 * $HeadURL: https://svn2.fit.ch001.ch:8443/svn/polyalert/trunk/02_Los_1/02_Production/polyalert-cc/src/server/com/com-common/src/test/java/ch/admin/babs/polyalert/server/com/common/util/JavaUtilTimeoutMapTest.java $
 *
 * $LastChangedDate: 2012-03-27 16:19:42 +0200 (Tue, 27 Mar 2012) $
 * $LastChangedBy: chamehl0 $
 * $LastChangedRevision: 24786 $
 */
package ch.bind.philib.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

public class JavaUtilTimeoutMapTest {

	private static final TimeUnit TU = TimeUnit.MILLISECONDS;

	@Test
	public void complexScenario() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();

		for (int i = 0; i < 1000; i++) {
			Integer ikv = Integer.valueOf(i);
			map.addWithTimestamp(i, TU, ikv, ikv);
		}
		assertEquals(map.size(), 1000);

		// find timeout entries 0-9
		long time = 0;
		assertNTimeouts(map, time, 10, 0);
		assertEquals(map.size(), 990);

		// find timeout entries 10-29
		time = 10;
		assertNTimeouts(map, time, 20, 10);
		assertEquals(map.size(), 97 * 10);

		// remove five entries (30-34)
		for (int i = 30; i < 35; i++) {
			map.remove(i);
		}
		assertEquals(map.size(), 965);

		time = 35;
		assertNTimeouts(map, time, 5, 35);
		assertEquals(map.size(), 960);

		time = 40;
		assertNTimeouts(map, time, 960, 40);

		assertTrue(map.isEmpty());
		assertNull(map.pollTimeout());
	}

	private void assertNTimeouts(TimeoutMap<Integer, Integer> map, long time, int numTimeouts, int firstKeyValue) {
		Integer keyValue = firstKeyValue;
		for (int i = 0; i < numTimeouts; i++) {
			assertTrue(map.containsKey(keyValue));
			Map.Entry<Integer, Integer> e = map.pollTimeout(time, TU);
			assertNotNull(e);
			assertEquals(e.getKey(), keyValue);
			assertEquals(e.getValue(), keyValue);

			assertFalse(map.containsKey(keyValue));
			assertNull(map.get(keyValue));
			assertNull(map.pollTimeout(time, TU));

			keyValue++;
			time++;
		}
	}

	@Test
	public void addEntry() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.add(0, TU, 123, 456);
		assertEquals(1, map.size());
		assertFalse(map.isEmpty());
		map.remove(123);
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	public void duplicateKeys() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		int key = 12345678;
		map.add(45, TimeUnit.SECONDS, key, 1);
		map.add(55, TimeUnit.SECONDS, key, 1234);
		assertEquals(map.get(key).intValue(), 1234);
		long timeToNextTimeout = map.getTimeToNextTimeout(TimeUnit.SECONDS);
		assertTrue(timeToNextTimeout == 55 || timeToNextTimeout == 54);
	}

	@Test
	public void returnNullOnNoTimeouts() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.add(10, TimeUnit.DAYS, 123, 456);
		assertFalse(map.isEmpty());
		assertNull(map.pollTimeout());
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "timeout must not be negative")
	public void addNoNegativeTimeout() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.add(-1, TU, 1, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "timestamp must not be negative")
	public void addNoNegativeTimestamp() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.addWithTimestamp(-1, TU, 1, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "time-unit must not be null")
	public void addNoNullTimeUnit1() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.add(0, null, 1, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "time-unit must not be null")
	public void addNoNullTimeUnit2() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.addWithTimestamp(0, null, 1, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "key must not be null")
	public void noNullKey1() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.add(0, TU, null, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "key must not be null")
	public void noNullKey2() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.addWithTimestamp(0, TU, null, 2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "value must not be null")
	public void noNullValue1() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.add(0, TU, 1, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "value must not be null")
	public void noNullValue2() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.addWithTimestamp(0, TU, 1, null);
	}
}
