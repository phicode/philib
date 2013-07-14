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
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
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
		URL[] urls = { URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.a"), //
		        URLs.forFile("/tmp/does-not-exist") };

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
