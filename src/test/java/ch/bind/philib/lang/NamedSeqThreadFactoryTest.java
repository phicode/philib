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

package ch.bind.philib.lang;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class NamedSeqThreadFactoryTest {

	@Test
	public void sequentialNumbers() {
		NamedSeqThreadFactory nstf = new NamedSeqThreadFactory("foo");
		Thread t = nstf.newThread(new R());
		assertFalse(t.isDaemon());
		assertEquals(t.getName(), "foo-0");
		assertEquals(t.getState(), Thread.State.NEW); // not started
		assertEquals(t.getThreadGroup(), Thread.currentThread().getThreadGroup());

		Thread t2 = nstf.newThread(new R());
		Thread t3 = nstf.newThread(new R());
		assertEquals(t2.getName(), "foo-1");
		assertEquals(t3.getName(), "foo-2");
	}

	@Test
	public void nullRunnable() {
		NamedSeqThreadFactory nstf = new NamedSeqThreadFactory("foo");
		assertNull(nstf.newThread(null));
	}

	@Test
	public void daemon() {
		NamedSeqThreadFactory nstf = new NamedSeqThreadFactory("foo", true);
		Thread t = nstf.newThread(new R());
		assertTrue(t.isDaemon());
		assertEquals(t.getName(), "foo-0");
		assertEquals(t.getState(), Thread.State.NEW); // not started
		assertEquals(t.getThreadGroup(), Thread.currentThread().getThreadGroup());
	}

	@Test
	public void group() {
		ThreadGroup group = new ThreadGroup("bar");
		NamedSeqThreadFactory nstf = new NamedSeqThreadFactory("foo", true, group);
		Thread t = nstf.newThread(new R());
		assertTrue(t.isDaemon());
		assertEquals(t.getName(), "foo-0");
		assertEquals(t.getState(), Thread.State.NEW); // not started
		assertEquals(t.getThreadGroup(), group);
	}

	private static final class R implements Runnable {

		@Override
		public void run() { /* NOOP */}
	}
}
