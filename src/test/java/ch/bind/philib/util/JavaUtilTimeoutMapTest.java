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

import org.testng.annotations.Test;

import ch.admin.babs.polyalert.server.com.common.util.TimeoutMap.TOEntry;

public class JavaUtilTimeoutMapTest {

	@Test
	public void complexScenario() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();

		int kv = 0;
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 10; j++) {
				map.add(i, kv, kv);
				kv++;
			}
		}
		assertEquals(map.size(), 100 * 10);

		// find ten entries (0-9) at time 0
		long time = 0;
		assertNTimeouts(map, time, 10, 0);
		assertEquals(map.size(), 99 * 10);

		// find ten entries (10-19) at time 1
		time = 1;
		assertNTimeouts(map, time, 10, 10);
		assertEquals(map.size(), 98 * 10);

		// find ten entries (20-29) at time 2
		time = 2;
		assertNTimeouts(map, time, 10, 20);
		assertEquals(map.size(), 97 * 10);

		// remove five entries (30-34)
		for (int i = 30; i < 35; i++) {
			map.remove(i);
		}
		assertEquals(map.size(), 97 * 10 - 5);

		// find five entries (35-39) at time 3
		time = 3;
		assertNTimeouts(map, time, 5, 35);
		assertEquals(map.size(), 96 * 10);
	}

	private void assertNTimeouts(TimeoutMap<Integer, Integer> map, long time, int numTimeouts, int firstKeyValue) {
		Integer keyValue = firstKeyValue;
		for (int i = 0; i < numTimeouts; i++) {
			System.out.println("testing " + i);
			TOEntry<Integer, Integer> e = map.findTimedout(time);
			assertNotNull(e);
			assertEquals(e.getTimeoutAt(), time);
			assertEquals(e.getKey(), keyValue);
			assertEquals(e.getValue(), keyValue);
			keyValue++;
		}
		// assert that we find no more timed-out entries for this time
		TOEntry<Integer, Integer> e = map.findTimedout(time);
		assertNull(e);
	}

	@Test
	public void addEntry() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		TOEntry<Integer, Integer> e = new TOEntry<Integer, Integer>(0, 123, 456);
		map.add(e);
		assertEquals(1, map.size());
		map.remove(123);
		assertTrue(map.isEmpty());
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "entry must not be null")
	public void nonNullEntry() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		TOEntry<Integer, Integer> e = null;
		map.add(e);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "timeoutAt must be > 0")
	public void nonNegativeTimeout() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.add(-1, 123, 456);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "timeoutAt must be > 0")
	public void nonNegativeTimeoutEntry() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		TOEntry<Integer, Integer> e = new TOEntry<Integer, Integer>(-1, 123, 456);
		map.add(e);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "key must not be null")
	public void nonNullKey() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.add(0, null, 456);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "key must not be null")
	public void nonNullKeyEntry() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		TOEntry<Integer, Integer> e = new TOEntry<Integer, Integer>(0, null, 456);
		map.add(e);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "value must not be null")
	public void nonNullValue() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		map.add(0, 123, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "value must not be null")
	public void nonNullValueEntry() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		TOEntry<Integer, Integer> e = new TOEntry<Integer, Integer>(0, 123, null);
		map.add(e);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "can not add duplicate key: 12345678")
	public void noDuplicateKeys() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		int key = 12345678;
		map.add(0, key, 1);
		map.add(55, key, 1234);
	}

	@Test
	public void returnNullOnNoTimeouts() {
		TimeoutMap<Integer, Integer> map = new JavaUtilTimeoutMap<Integer, Integer>();
		long now = System.currentTimeMillis();
		map.add(now + 1000, 123, 456);
		assertNull(map.findTimedout());
		assertFalse(map.isEmpty());
	}
}
