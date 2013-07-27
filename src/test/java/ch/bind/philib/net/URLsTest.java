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

package ch.bind.philib.net;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.net.URL;

import org.testng.annotations.Test;

public class URLsTest {

	@Test
	public void forClasspathResource() {
		URL a = URLs.forClasspathResource("/words_en");
		URL b = URLs.forClasspathResource("missing");
		assertNotNull(a);
		assertNull(b);
	}

	// platform dependent
	@Test(enabled = false)
	public void forFile() throws IOException {
		URL a = URLs.forFile("foo"); // relative
		URL b = URLs.forFile("/bar"); // absolute

		String workDir = System.getProperty("user.dir");
		// workDir = workDir.replace('\\', '/');
		// workDir = workDir.startsWith("/") ? workDir : "/" + workDir;
		assertEquals(a.toString(), "file:" + workDir + "/foo");

		assertEquals(b.toExternalForm(), "file:/bar");
	}
}