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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class FilteringListenerTest {

	@Test
	public void whiteBlackList() {
		ConfigListener l = mock(ConfigListener.class);
		FilteringListener f = new FilteringListener(l);
		assertTrue(f.isBlacklist());

		f.addFilterProperty("b");

		assertFalse(f.filterMatch("a"));
		assertTrue(f.filterMatch("b"));
		assertFalse(f.filterMatch("c"));

		f.setBlacklist(false);

		assertTrue(f.filterMatch("a"));
		assertFalse(f.filterMatch("b"));
		assertTrue(f.filterMatch("c"));

		f.removeFilterProperty("b");
		assertTrue(f.filterMatch("b"));

		verifyNoMoreInteractions(l);
	}

	@Test
	public void includeAdditions() {
		ConfigListener l = mock(ConfigListener.class);
		FilteringListener f = new FilteringListener(l);
		assertTrue(f.isIncludeAdditions());

		f.added("a", "a");

		f.setIncludeAdditions(false);

		f.added("b", "b");

		verify(l).added("a", "a");
		verifyNoMoreInteractions(l);
	}

	@Test
	public void includeChanges() {
		ConfigListener l = mock(ConfigListener.class);
		FilteringListener f = new FilteringListener(l);
		assertTrue(f.isIncludeChanges());

		f.changed("a", "a1", "a2");

		f.setIncludeChanges(false);

		f.changed("b", "b1", "b2");

		verify(l).changed("a", "a1", "a2");
		verifyNoMoreInteractions(l);
	}

	@Test
	public void includeRemovals() {
		ConfigListener l = mock(ConfigListener.class);
		FilteringListener f = new FilteringListener(l);
		assertTrue(f.isIncludeRemovals());

		f.removed("a", "a");

		f.setIncludeRemovals(false);

		f.removed("b", "b");

		verify(l).removed("a", "a");
		verifyNoMoreInteractions(l);
	}
}
