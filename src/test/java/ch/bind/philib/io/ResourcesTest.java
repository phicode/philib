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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.testng.annotations.Test;

public class ResourcesTest {

	@Test(expectedExceptions = FileNotFoundException.class)
	public void openResourceClasspathNotFound() throws IOException {
		Resources.openResource("classpath:/does-not-exist");
	}

	@Test
	public void openResourceClasspath() throws IOException {
		try (InputStream is = Resources.openResource("classpath:/words_en")) {
			assertNotNull(is);
		}
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void openResourceFileNotFound() throws IOException {
		Resources.openResource("/does-not-exist");
	}

	@Test
	public void openResourceFile() throws IOException {
		try (InputStream is = Resources.openResource("./src/test/resources/words_en")) {
			assertNotNull(is);
			assertEquals(is.getClass(), FileInputStream.class);
		}
	}
}
