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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import ch.bind.philib.net.URLs;

public class SyspropOverridingConfigTest {

	@Test
	public void loadMap() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("aa", "11");
		SyspropOverridingConfig c = new SyspropOverridingConfig(map);
		assertEquals(c.getInt("aa").intValue(), 11);
		c.load();
		assertEquals(c.getInt("aa").intValue(), 11);
	}

	@Test
	public void load() throws IOException {
		URL url = URLs.forClasspathResource("/words_en");
		SyspropOverridingConfig c = new SyspropOverridingConfig(url);
		c.load();
	}

	@Test
	public void loadIgnoreNull() throws IOException {
		URL url = URLs.forClasspathResource("/words_en");
		SyspropOverridingConfig c = new SyspropOverridingConfig(Arrays.asList((URL) null, url));
		c.load();
	}

	@Test
	public void loadMultiple() throws IOException {
		URL[] urls = {URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.a"), //
				URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.b")};
		SyspropOverridingConfig c = new SyspropOverridingConfig(urls);
		c.load();
	}

	@Test
	public void shouldOverride() throws IOException {
		URL url = URLs.forClasspathResource("/ch/bind/philib/config/ConfigTest.a");
		SyspropOverridingConfig c = new SyspropOverridingConfig(url);
		c.load();

		// value from the config
		assertEquals(c.getInt("a").intValue(), 1);

		// override
		System.setProperty("a", "99");
		assertEquals(c.getInt("a").intValue(), 99);
		assertEquals(c.getInt("a", 0), 99);
	}
}
