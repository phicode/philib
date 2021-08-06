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

package ch.bind.philib.io;

import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.testng.annotations.Test;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertEquals;

public class SafeCloseUtilTest {

	@Test
	public void closeable() {
		C c = new C(false);
		SafeCloseUtil.close(c);
		assertEquals(c.numCalls, 1);
	}

	@Test
	public void closeableNotImplemented() {
		NonCloseable o = new NonCloseable(false);
		SafeCloseUtil.close(o);
		assertEquals(o.numCalls, 1);
	}

	@Test
	public void closeableExc() {
		C c = new C(true);
		Logger l = mock(Logger.class);
		SafeCloseUtil.close(c, l);
		assertEquals(c.numCalls, 1);
		verify(l).error(anyString(), any(IOException.class));
		verifyNoMoreInteractions(l);
	}

	@Test
	public void closeableNotImplementedExc() {
		NonCloseable o = new NonCloseable(true);
		Logger l = mock(Logger.class);
		SafeCloseUtil.close(o, l);
		assertEquals(o.numCalls, 1);
		verify(l).error(anyString(), any(InvocationTargetException.class));
		verifyNoMoreInteractions(l);
	}

	@Test
	public void dontFailOnNull() {
		SafeCloseUtil.close((Closeable) null);
		SafeCloseUtil.close((Object) null);
	}

	@Test
	public void dontFailOnNoCloseMethod() {
		SafeCloseUtil.close(new Object(), NOPLogger.NOP_LOGGER);
	}

	private static final class C implements Closeable {

		final boolean doThrow;

		int numCalls;

		C(boolean doThrow) {
			this.doThrow = doThrow;
		}

		@Override
		public void close() throws IOException {
			numCalls++;
			if (doThrow) {
				throw new IOException("testing " + SafeCloseUtilTest.class.getSimpleName());
			}
		}
	}

	private static final class NonCloseable {

		final boolean doThrow;

		int numCalls;

		NonCloseable(boolean doThrow) {
			this.doThrow = doThrow;
		}

		@SuppressWarnings("unused")
		public void close() throws IOException {
			numCalls++;
			if (doThrow) {
				throw new IOException("testing " + SafeCloseUtilTest.class.getSimpleName());
			}
		}
	}
}
