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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

public class LoggingListenerTest {

	@Test
	public void defaultConstructor() {
		LoggingListener ll = new LoggingListener();
		assertNotNull(ll.getLogger());
		assertSame(ll.getLogger(), LoggerFactory.getLogger(LoggingListener.class));
	}

	@Test
	public void added() {
		Logger log = mock(Logger.class);
		LoggingListener ll = new LoggingListener(log);
		ll.added("key", "value");
		verify(log).info("added '%s': '%s'", "key", "value");
		verifyNoMoreInteractions(log);
	}

	@Test
	public void changed() {
		Logger log = mock(Logger.class);
		LoggingListener ll = new LoggingListener(log);
		ll.changed("key", "oldValue", "newValue");
		verify(log).info("changed '%s': '%s' -> '%s'", "key", "oldValue", "newValue");
		verifyNoMoreInteractions(log);
	}

	@Test
	public void removed() {
		Logger log = mock(Logger.class);
		LoggingListener ll = new LoggingListener(log);
		ll.removed("key", "oldValue");
		verify(log).info("removed '%s': '%s'", "key", "oldValue");
		verifyNoMoreInteractions(log);
	}
}
