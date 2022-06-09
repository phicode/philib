/*
 * Copyright (c) 2006-2022 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import ch.bind.philib.conf.Config;

public abstract class Resources {

	protected Resources() {
	}

	/**
	 * Opens different resources based on the scheme being used.
	 * <p>
	 * <h2>Classpath</h2>
	 * <p></p>
	 * Resources starting with <b>classpath:</b> have this prefix removed and all
	 * <ul>
	 * <li>classpath:</li>
	 * </ul>
	 *
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public static InputStream openResource(String resource) throws IOException {
		if (resource.startsWith("classpath:")) {
			String cp = resource.substring("classpath:".length());
			InputStream is = Resources.class.getResourceAsStream(cp);
			if (is == null) {
				throw new FileNotFoundException("classpath resource not found: " + resource);
			}
			return is;
		}
		return new FileInputStream(resource);
	}

	public static Map<String, String> loadPropertiesFile(String resource) throws IOException {
		InputStream is = openResource(resource);
		try {
			Properties props = new Properties();
			props.load(is);
			return Config.toMap(props);
		} finally {
			SafeCloseUtil.close(is);
		}
	}
}
