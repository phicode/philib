/*
 * Copyright (c) 2006-2009 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.cache;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * JUnit tests for the <code>MemoryObjectCache</code>.
 * 
 * @author Philipp Meinen
 * @since 2006-10-14
 * @version 0.1
 * @see MemoryObjectCache
 */
public class TestMemoryObjectCache {

    @Test
    public void getCapacity() {
        MemoryObjectCache<Integer, Integer> cache;

        cache = new MemoryObjectCache<Integer, Integer>();
        assertEquals("invalid capacity",
                MemoryObjectCache.DEFAULT_CACHE_CAPACITY, cache.getCapacity());

        cache = new MemoryObjectCache<Integer, Integer>(1024);
        assertEquals("invalid capacity", 1024, cache.getCapacity());

        cache = new MemoryObjectCache<Integer, Integer>(
                MemoryObjectCache.MIN_CACHE_CAPACITY - 1);
        assertEquals("invalid capacity", MemoryObjectCache.MIN_CACHE_CAPACITY,
                cache.getCapacity());

        cache = new MemoryObjectCache<Integer, Integer>(0);
        assertEquals("invalid capacity", MemoryObjectCache.MIN_CACHE_CAPACITY,
                cache.getCapacity());
    }

    @Test
    public void fullCacheWhereOldObjectGetRemoved() {
        final int testSize = MemoryObjectCache.DEFAULT_CACHE_CAPACITY;

        MemoryObjectCache<String, String> cache = new MemoryObjectCache<String, String>(
                testSize);
        RecyleListenerTester<String, String> listener = new RecyleListenerTester<String, String>();
        cache.addRecycleListener(listener);

        assertEquals(0, cache.size());
        assertEquals(testSize, cache.getCapacity());

        for (int i = 1; i <= testSize; i++) {
            cache.add(Integer.toString(i), Integer.toString(i * i * i));
        }

        assertEquals(testSize, cache.size());
        // the key 1, value 1 falls away
        listener.addExpectedRecyclePair("1", "1");
        cache.add("-1", "-1");
        assertEquals(testSize, cache.size());

        for (int i = 2; i <= testSize; i++) {
            String v = cache.get(Integer.toString(i));
            assertEquals(Integer.toString(i * i * i), v);
        }

        assertEquals("-1", cache.get("-1"));

        // the key 2, value 8 falls away
        listener.addExpectedRecyclePair("2", "8");
        cache.add("-2", "-2");
        assertEquals(testSize, cache.size());

        for (int i = 3; i <= testSize; i++) {
            String v = cache.get(Integer.toString(i));
            assertEquals(Integer.toString(i * i * i), v);
        }

        assertEquals("-1", cache.get("-1"));
        assertEquals("-2", cache.get("-2"));

        assertEquals(testSize, cache.size());
        assertEquals(testSize, cache.getCapacity());
    }

    @Test
    public void fullCacheWhereOldObjectGetRemoved2() {
        final int testSize = 10000;

        MemoryObjectCache<String, String> cache = new MemoryObjectCache<String, String>(
                testSize);
        RecyleListenerTester<String, String> listener = new RecyleListenerTester<String, String>();
        cache.addRecycleListener(listener);

        assertEquals(0, cache.size());
        assertEquals(testSize, cache.getCapacity());

        for (int i = 1; i <= testSize; i++) {
            cache.add(Integer.toString(i), Integer.toString(i * i));
        }

        assertEquals(testSize, cache.size());

        // query the elements from 5000 to 8999 (4000 elements) so that
        // they are marked as beeing accessed recently
        for (int i = 5000; i <= 8999; i++) {
            String v = cache.get(Integer.toString(i));
            assertEquals(Integer.toString(i * i), v);
        }

        // insert 6000 new elements
        // => 1-4999 and 9000-testSize get removed
        for (int i = 1; i < 5000; i++)
            listener.addExpectedRecyclePair(Integer.toString(i), Integer
                    .toString(i * i));
        for (int i = 9000; i <= testSize; i++)
            listener.addExpectedRecyclePair(Integer.toString(i), Integer
                    .toString(i * i));
        for (int i = 10001; i <= 16000; i++)
            cache.add(Integer.toString(i), Integer.toString(i * i));

        // elements 1 to 4999 == null
        for (int i = 1; i < 5000; i++) {
            String v = cache.get(Integer.toString(i));
            assertNull(v);
        }
        // elements 9000 to testSize == null
        for (int i = 9000; i <= testSize; i++) {
            String v = cache.get(Integer.toString(i));
            assertNull(v);
        }
        // elements 5000 to 8999 are present
        for (int i = 5000; i < 9000; i++) {
            String v = cache.get(Integer.toString(i));
            assertNotNull(v);
            assertEquals(Integer.toString(i * i), v);
        }
        // elements 10001 to 16000 are present
        for (int i = 10001; i <= 16000; i++) {
            String v = cache.get(Integer.toString(i));
            assertNotNull(v);
            assertEquals(Integer.toString(i * i), v);
        }

        assertEquals(testSize, cache.size());
        assertEquals(testSize, cache.getCapacity());
    }

    @Test
    public void fullCacheWhereOldObjectGetRemoved3() {
        MemoryObjectCache<String, String> cache = new MemoryObjectCache<String, String>(
                100000);
        RecyleListenerTester<String, String> listener = new RecyleListenerTester<String, String>();
        cache.addRecycleListener(listener);

        assertEquals(0, cache.size());
        assertEquals(100000, cache.getCapacity());

        for (int i = 1; i <= 100000; i++)
            cache.add(Integer.toString(i), Integer.toString(i * i));

        assertEquals(100000, cache.size());

        // query every second element so that
        // they are marked as beeing accessed recently
        for (int i = 2; i <= 100000; i += 2) {
            String v = cache.get(Integer.toString(i));
            assertEquals(Integer.toString(i * i), v);
        }

        // insert 50000 new elements
        // => all odd numbers from 1-100000 get removed
        for (int i = 1; i < 100000; i += 2)
            listener.addExpectedRecyclePair(Integer.toString(i), Integer
                    .toString(i * i));
        for (int i = 100001; i <= 150000; i++)
            cache.add(Integer.toString(i), Integer.toString(i * i));

        // all odd numbers from 1-100000 are null
        for (int i = 1; i < 100000; i += 2) {
            String v = cache.get(Integer.toString(i));
            assertNull(v);
        }
        // all even numbers are present
        for (int i = 2; i <= 100000; i += 2) {
            String v = cache.get(Integer.toString(i));
            assertNotNull(v);
            assertEquals(Integer.toString(i * i), v);
        }
        // elements 100001 to 150000 are present
        for (int i = 100001; i <= 150000; i++) {
            String v = cache.get(Integer.toString(i));
            assertNotNull(v);
            assertEquals(Integer.toString(i * i), v);
        }

        assertEquals(100000, cache.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void toSmallTimeout() {
        new MemoryObjectCache<String, String>(
                MemoryObjectCache.DEFAULT_CACHE_CAPACITY, -1L);
    }

    @Test
    public void timeout() {
        // a cache with 0.1 sec timeout
        ICache<String, String> cache = new MemoryObjectCache<String, String>(
                MemoryObjectCache.MIN_CACHE_CAPACITY, 100);
        RecyleListenerTester<String, String> listener = new RecyleListenerTester<String, String>();
        cache.addRecycleListener(listener);

        cache.add("1", "one");
        cache.add("2", "two");
        cache.add("3", "three");
        cache.add("4", "four");
        cache.add("5", "five");
        assertEquals(5, cache.size());
        assertEquals("one", cache.get("1"));
        assertEquals("two", cache.get("2"));
        assertEquals("three", cache.get("3"));
        assertEquals("four", cache.get("4"));
        assertEquals("five", cache.get("5"));

        // after 0.05 sec these elements should still be in the cache
        try {
            Thread.sleep(50); // sleep 0.05 sec
        } catch (InterruptedException exc) {
            fail("interrupted while sleeping :)");
        }

        cache.add("6", "six");
        assertEquals(6, cache.size());

        // wait 0.06 sec
        // total wait time: 0.11 sec + the program exec time
        try {
            Thread.sleep(60); // sleep 0.06 sec
        } catch (InterruptedException exc) {
            fail("interrupted while sleeping :)");
        }

        // the first five elements of the cache should be timed
        // out by now. only the Pair "6"-"six" should be in the cache
        listener.addExpectedRecyclePair("1", "one");
        listener.addExpectedRecyclePair("2", "two");
        listener.addExpectedRecyclePair("3", "three");
        listener.addExpectedRecyclePair("4", "four");
        listener.addExpectedRecyclePair("5", "five");
        assertEquals(1, cache.size());
        assertNull(null, cache.get("1"));
        assertNull(null, cache.get("2"));
        assertNull(null, cache.get("3"));
        assertNull(null, cache.get("4"));
        assertNull(null, cache.get("5"));
        assertEquals("six", cache.get("6"));

        try {
            Thread.sleep(50); // sleep 0.05 sec
        } catch (InterruptedException exc) {
            fail("interrupted while sleeping :)");
        }
        listener.addExpectedRecyclePair("6", "six");
        assertNull(null, cache.get("6"));
        assertEquals(0, cache.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNullKey() {
        ICache<String, String> cache = new MemoryObjectCache<String, String>();
        cache.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullKey() {
        ICache<String, String> cache = new MemoryObjectCache<String, String>();
        cache.add(null, "abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNullKey() {
        ICache<String, String> cache = new MemoryObjectCache<String, String>();
        cache.remove(null);
    }

    @Test
    public void isEmpty() {
        ICache<String, String> cache = new MemoryObjectCache<String, String>();
        RecyleListenerTester<String, String> listener = new RecyleListenerTester<String, String>();
        cache.addRecycleListener(listener);

        assertTrue(cache.isEmpty());
        cache.add("1", "one");
        assertFalse(cache.isEmpty());

        cache.remove("2");
        assertFalse(cache.isEmpty());

        listener.addExpectedRecyclePair("1", "one");
        cache.remove("1");
        assertTrue(cache.isEmpty());
    }

    @Test
    public void timeoutWithIsEmpty() {
        // 100 ms timeout cache
        ICache<String, String> cache = new MemoryObjectCache<String, String>(
                MemoryObjectCache.DEFAULT_CACHE_CAPACITY, 100);
        RecyleListenerTester<String, String> listener = new RecyleListenerTester<String, String>();
        cache.addRecycleListener(listener);

        assertTrue(cache.isEmpty());
        cache.add("1", "one");
        cache.clearTimedOutPairs();
        assertFalse(cache.isEmpty());
        try {
            Thread.sleep(60);
            cache.clearTimedOutPairs();
            assertFalse(cache.isEmpty());

            Thread.sleep(60);
            listener.addExpectedRecyclePair("1", "one");
            cache.clearTimedOutPairs();
            assertTrue(cache.isEmpty());
        } catch (InterruptedException exc) {
            fail("interrupted");
        }
    }

    @Test
    public void contains() {
        ICache<String, String> cache = new MemoryObjectCache<String, String>();
        RecyleListenerTester<String, String> listener = new RecyleListenerTester<String, String>();
        cache.addRecycleListener(listener);

        assertFalse(cache.contains("1"));
        cache.add("1", "one");
        assertTrue(cache.contains("1"));

        cache.remove("2");
        assertTrue(cache.contains("1"));

        listener.addExpectedRecyclePair("1", "one");
        cache.remove("1");
        assertFalse(cache.contains("1"));
    }

    @Test
    public void timeoutWithContains() {
        // 100 ms timeout cache
        ICache<String, String> cache = new MemoryObjectCache<String, String>(
                MemoryObjectCache.DEFAULT_CACHE_CAPACITY, 100);
        RecyleListenerTester<String, String> listener = new RecyleListenerTester<String, String>();
        cache.addRecycleListener(listener);

        assertFalse(cache.contains("1"));
        cache.add("1", "one");
        cache.clearTimedOutPairs();
        assertTrue(cache.contains("1"));
        try {
            Thread.sleep(60);
            cache.clearTimedOutPairs();
            assertTrue(cache.contains("1"));

            Thread.sleep(60);
            listener.addExpectedRecyclePair("1", "one");
            cache.clearTimedOutPairs();
            assertFalse(cache.contains("1"));
        } catch (InterruptedException exc) {
            fail("interrupted");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void containsWithNull() {
        ICache<String, String> cache = new MemoryObjectCache<String, String>();
        cache.contains(null);
    }

    @Test
    public void addMultiple() {
        ICache<String, String> cache = new MemoryObjectCache<String, String>();
        cache.add("1", "one version 1");
        cache.add("1", "one version 2");
        assertEquals(1, cache.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void serialization() throws IOException, ClassNotFoundException {
        MemoryObjectCache<String, String> cache = new MemoryObjectCache<String, String>();
        cache.add("1", "one");
        cache.add("2", "two");
        cache.add("3", "three");
        cache.add("4", "four");
        cache.add("5", "five");

        File tmpFile = File.createTempFile("junit-serialization-test", ".ser");
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(cache);
        oos.flush();
        oos.close();
        fos.flush();
        fos.close();

        FileInputStream fis = new FileInputStream(tmpFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        cache = (MemoryObjectCache<String, String>) ois.readObject();
        ois.close();
        fis.close();

        assertEquals(MemoryObjectCache.DEFAULT_CACHE_CAPACITY, cache
                .getCapacity());
        assertEquals(MemoryObjectCache.DEFAULT_TIMEOUT, cache.getTimeout());
        assertEquals(5, cache.size());
        assertEquals("one", cache.get("1"));
        assertEquals("two", cache.get("2"));
        assertEquals("three", cache.get("3"));
        assertEquals("four", cache.get("4"));
        assertEquals("five", cache.get("5"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void serializationWithTimeout() throws IOException,
            ClassNotFoundException {
        // 100 millisec timeout
        MemoryObjectCache<String, String> cache = new MemoryObjectCache<String, String>(
                MemoryObjectCache.DEFAULT_CACHE_CAPACITY, 100);
        cache.add("1", "one");
        cache.add("2", "two");
        cache.add("3", "three");
        cache.add("4", "four");
        cache.add("5", "five");

        File tmpFile = File.createTempFile("junit-serialization-test", ".ser");
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(cache);
        oos.flush();
        oos.close();
        oos = null;
        fos.flush();
        fos.close();
        fos = null;
        cache = null;

        FileInputStream fis = new FileInputStream(tmpFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        cache = (MemoryObjectCache<String, String>) ois.readObject();
        ois.close();
        ois = null;
        fis.close();
        fis = null;

        assertEquals(MemoryObjectCache.DEFAULT_CACHE_CAPACITY, cache
                .getCapacity());
        assertEquals(100, cache.getTimeout());
        assertEquals(5, cache.size());
        assertEquals("one", cache.get("1"));
        assertEquals("two", cache.get("2"));
        assertEquals("three", cache.get("3"));
        assertEquals("four", cache.get("4"));
        assertEquals("five", cache.get("5"));

        cache = null;

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            fail("interrupted");
        }

        fis = new FileInputStream(tmpFile);
        ois = new ObjectInputStream(fis);
        cache = (MemoryObjectCache<String, String>) ois.readObject();
        ois.close();
        ois = null;
        fis.close();
        fis = null;

        assertEquals(MemoryObjectCache.DEFAULT_CACHE_CAPACITY, cache
                .getCapacity());
        assertEquals(100, cache.getTimeout());
        assertEquals(0, cache.size());
    }

    private static final class RecyleListenerTester<K, V> implements
            RecycleListener<K, V> {

        int pos;
        List<K> keys = new ArrayList<K>();
        List<V> values = new ArrayList<V>();

        void addExpectedRecyclePair(K k, V v) {
            keys.add(k);
            values.add(v);
        }

        public void onRecyclePair(K key, V value) {
            String msg = "pair removed: (" + key + "; " + value + ')';
            assertTrue(msg, keys.size() > pos);
            assertEquals(msg, keys.get(pos), key);
            assertEquals(msg, values.get(pos), value);
            pos++;
        }

    }
}
