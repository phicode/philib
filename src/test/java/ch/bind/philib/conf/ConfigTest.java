/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.conf;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.bind.philib.net.URLs;

public class ConfigTest {

    @Test
    public void load() throws IOException {
        URL url = URLs.forClasspathResource("/words_en");
        Config c = new Config(url);
        c.load();
    }

    @Test
    public void loadIgnoreNull() throws IOException {
        URL url = URLs.forClasspathResource("/words_en");
        Config c = new Config(Arrays.asList((URL) null, url));
        c.load();
    }

    @Test
    public void loadWithNotify() throws IOException {
        URL url = URLs.forClasspathResource("/words_en");
        Config c = new Config(url);
        RecordingConfigValueListener l = new RecordingConfigValueListener();
        c.addListener(l);
        c.load();
        assertTrue(l.changed.isEmpty());
        assertTrue(l.removed.isEmpty());
        assertTrue(l.added.size() > 200000);
    }

    @Test
    public void loadMultiple() throws IOException {
        URL[] urls = { URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.a"), //
                URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.b") };

        Config c = new Config(urls);

        RecordingConfigValueListener l = new RecordingConfigValueListener();
        c.addListener(l);
        c.load();
        assertTrue(l.changed.isEmpty());
        assertTrue(l.removed.isEmpty());
        assertEquals(l.added.size(), 4);
        assertEquals(l.current.size(), 4);
        assertTrue(l.current.containsKey("a") && l.current.get("a").equals("1"));
        assertTrue(l.current.containsKey("b") && l.current.get("b").equals("22"));
        assertTrue(l.current.containsKey("c") && l.current.get("c").equals("3"));
        assertTrue(l.current.containsKey("d") && l.current.get("d").equals("4"));
    }

    @Test
    public void oneResource() throws IOException {
        List<URL> urls = Arrays.asList(URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.a"), //
                URLs.forFile("/tmp/does-not-exist"));

        Config c = new Config(urls);

        RecordingConfigValueListener l = new RecordingConfigValueListener();
        c.addListener(l);
        c.load();
        assertTrue(l.changed.isEmpty());
        assertTrue(l.removed.isEmpty());
        assertEquals(l.added.size(), 3);
        assertEquals(l.current.size(), 3);
        assertTrue(l.current.containsKey("a") && l.current.get("a").equals("1"));
        assertTrue(l.current.containsKey("b") && l.current.get("b").equals("2"));
        assertTrue(l.current.containsKey("c") && l.current.get("c").equals("3"));
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "no resources found")
    public void atLeastOneResource() throws IOException {
        URL[] urls = { URLs.forFile("/tmp/does-not-exist-1"), //
                URLs.forFile("/tmp/does-not-exist-2") };

        Config c = new Config(urls);
        c.load();
    }

    @Test
    public void get() throws IOException {
        URL url = URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.a");
        Config c = new Config(url);
        c.load();
        assertEquals(c.get("a"), "1");
        assertNull(c.get("foo"));

        assertNotNull(c.getInt("a"));
        assertTrue(c.getInt("a").intValue() == 1);
        assertNull(c.getInt("foo"));

        assertNotNull(c.getLong("a"));
        assertTrue(c.getLong("a").longValue() == 1L);
        assertNull(c.getLong("foo"));
    }

    @Test
    public void getWithDefault() throws IOException {
        URL url = URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.a");
        Config c = new Config(url);
        c.load();
        assertEquals(c.get("a", "notfound"), "1");
        assertEquals(c.get("foo", "notfound"), "notfound");

        assertTrue(c.getInt("a", 1234) == 1);
        assertTrue(c.getInt("foo", 1234) == 1234);

        assertTrue(c.getLong("a", 1234) == 1L);
        assertTrue(c.getLong("foo", 1234) == 1234);
    }

    @Test
    public void changingConfig() throws IOException {
        URL urla = URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.a");
        URL urlb = URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.b");
        Config c = new Config(urla);
        RecordingConfigValueListener l = new RecordingConfigValueListener();
        c.addListener(l);
        c.load();
        assertEquals(c.get("a"), "1");
        assertEquals(c.get("b"), "2");
        assertEquals(c.get("c"), "3");
        assertEquals(l.added.size(), 3);
        assertEquals(l.removed.size(), 0);
        assertEquals(l.changed.size(), 0);

        l.clearNotifications();
        c.setURL(urlb);
        c.load();

        assertEquals(c.get("a"), "1");
        assertEquals(c.get("b"), "22");
        assertEquals(c.get("d"), "4");

        assertEquals(l.added.size(), 1);
        assertTrue(l.added.contains("d"));

        assertEquals(l.removed.size(), 1);
        assertTrue(l.removed.contains("c"));

        assertEquals(l.changed.size(), 1);
        assertTrue(l.changed.contains("b"));
    }

    private static final class RecordingConfigValueListener implements ConfigValueListener {

        Map<String, String> current = new HashMap<String, String>();
        Set<String> changed = new HashSet<String>();
        Set<String> removed = new HashSet<String>();
        Set<String> added = new HashSet<String>();

        @Override
        public void changed(String key, String oldValue, String newValue) {
            changed.add(key);
            current.put(key, newValue);
        }

        public void clearNotifications() {
            changed.clear();
            removed.clear();
            added.clear();
        }

        @Override
        public void removed(String key, String oldValue) {
            removed.add(key);
            current.remove(key);
        }

        @Override
        public void added(String key, String value) {
            added.add(key);
            current.put(key, value);
        }
    }
}
