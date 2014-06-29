/*
 * Copyright (c) 2006-2011 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.lang;

import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ExceptionUtilTest {

	@Test
	public void buildMessageChain() {
		Exception e = null;
		try {
			try {
				throw new NullPointerException("NPE");
			} catch (Exception inner) {
				throw new IllegalArgumentException("IAE", inner);
			}
		} catch (Exception outer) {
			e = outer;
		}
		assertNotNull(e);
		String p = "^ExceptionUtilTest\\.buildMessageChain:[0-9]+#IllegalArgumentException\\(IAE\\) => " + //
				"ExceptionUtilTest\\.buildMessageChain:[0-9]+#NullPointerException\\(NPE\\)$";
		String out = ExceptionUtil.buildMessageChain(e);
		assertTrue(Pattern.matches(p, out));
	}

	@Test
	public void handleNull() {
		Exception e = new NullPointerException();
		assertNotNull(e);
		String p = "^ExceptionUtilTest\\.handleNull:[0-9]+#NullPointerException\\(\\)$";
		String out = ExceptionUtil.buildMessageChain(e);
		assertTrue(Pattern.matches(p, out));
	}

	@Test
	public void emptyOnNull() {
		assertEquals(ExceptionUtil.buildMessageChain(null), "");
	}
}
