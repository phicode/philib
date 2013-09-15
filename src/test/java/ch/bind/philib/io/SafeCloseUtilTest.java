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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertEquals;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

import org.slf4j.Logger;
import org.testng.annotations.Test;

public class SafeCloseUtilTest {

	@Test
	public void closeable() {
		C c = new C(false);
		SafeCloseUtil.close(c);
		assertEquals(c.numCalls, 1);
	}

	@Test
	public void selector() {
		Sel s = new Sel(false);
		SafeCloseUtil.close(s);
		assertEquals(s.numCalls, 1);
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
	public void selectorExc() {
		Sel s = new Sel(true);
		Logger l = mock(Logger.class);
		SafeCloseUtil.close(s, l);
		assertEquals(s.numCalls, 1);
		verify(l).error(anyString(), any(IOException.class));
		verifyNoMoreInteractions(l);
	}

	@Test
	public void dontFailOnNull() {
		SafeCloseUtil.close((Closeable) null);
		SafeCloseUtil.close((Selector) null);
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

	private static final class Sel extends Selector {

		final boolean doThrow;

		int numCalls;

		Sel(boolean doThrow) {
			this.doThrow = doThrow;
		}

		@Override
		public void close() throws IOException {
			numCalls++;
			if (doThrow) {
				throw new IOException("testing " + SafeCloseUtilTest.class.getSimpleName());
			}
		}

		@Override
		public boolean isOpen() {
			throw new AssertionError();
		}

		@Override
		public SelectorProvider provider() {
			throw new AssertionError();
		}

		@Override
		public Set<SelectionKey> keys() {
			throw new AssertionError();
		}

		@Override
		public Set<SelectionKey> selectedKeys() {
			throw new AssertionError();
		}

		@Override
		public int selectNow() throws IOException {
			throw new AssertionError();
		}

		@Override
		public int select(long timeout) throws IOException {
			throw new AssertionError();
		}

		@Override
		public int select() throws IOException {
			throw new AssertionError();
		}

		@Override
		public Selector wakeup() {
			throw new AssertionError();
		}
	}
}
