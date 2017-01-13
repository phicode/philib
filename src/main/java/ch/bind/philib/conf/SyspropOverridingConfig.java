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

import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * same behaviour as Config with the exception that, if a system property with the requested key exists, it takes precedence over the value from the config or
 * the default value.
 */
public class SyspropOverridingConfig extends Config {

	public SyspropOverridingConfig(Collection<URL> urls) {
		super(urls);
	}

	public SyspropOverridingConfig(Map<String, String> config) {
		super(config);
	}

	public SyspropOverridingConfig(URL url) {
		super(url);
	}

	public SyspropOverridingConfig(URL[] urls) {
		super(urls);
	}

	@Override
	public String get(String key) {
		String v = System.getProperty(key);
		return v == null ? super.get(key) : v;
	}
}
