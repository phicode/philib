/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.net.core.events;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.testng.annotations.Test;

public class SimpleEventDispatcherTest {

	// EventDispatcher methods
	// boolean isOpen();
	// void register(EventHandler eventHandler, int ops);
	// void unregister(EventHandler eventHandler);
	// void setTimeout(EventHandler eventHandler, long timeout);
	// void unsetTimeout(EventHandler eventHandler);
	// int getRegisteredOps(EventHandler eventHandler);
	// int getNumEventHandlers();
	// long getLoadAvg();

	@Test(timeOut = 1000)
	public void openAndClose() throws Exception {
		DummySelectorProvider selectorProvider = new DummySelectorProvider();
		DummySelector selector = new DummySelector(selectorProvider);
		selectorProvider.setNextOpenSelector(selector);

		SimpleEventDispatcher dispatcher = SimpleEventDispatcher.open(selectorProvider, false);

		assertTrue(dispatcher.isOpen());
		assertEquals(dispatcher.getNumEventHandlers(), 0);
		assertEquals(dispatcher.getLoadAvg(), 0);

		dispatcher.close();

		assertFalse(dispatcher.isOpen());
	}

	@Test(timeOut = 1000, expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "unable to register an event-handler on a unopen event-dispatcher")
	public void noRegistersAfterClose() throws Exception {
		DummySelectorProvider selectorProvider = new DummySelectorProvider();
		DummySelector selector = new DummySelector(selectorProvider);
		selectorProvider.setNextOpenSelector(selector);

		SimpleEventDispatcher dispatcher = SimpleEventDispatcher.open(selectorProvider, false);
		dispatcher.close();

		assertFalse(dispatcher.isOpen());

		EventHandler handler = new DummyEventHandler(0, new DummySelectableChannel(selectorProvider));
		dispatcher.register(handler, 0);
	}

	@Test(timeOut = 1000)
	public void dispatchEvents() throws Exception {
		DummySelectorProvider selectorProvider = new DummySelectorProvider();
		DummySelector selector = new DummySelector(selectorProvider);
		selectorProvider.setNextOpenSelector(selector);

		SimpleEventDispatcher dispatcher = SimpleEventDispatcher.open(selectorProvider, false);

		DummySelectableChannel channel = new DummySelectableChannel(selectorProvider);
		DummyEventHandler handler = new DummyEventHandler(0, channel);
		dispatcher.register(handler, 1);

//		while (selector.registerCalls < 1) {
//			Thread.yield();
//		}

		dispatcher.close();

		assertFalse(dispatcher.isOpen());
		assertEquals(handler.closeCalls, 1);
		assertEquals(dispatcher.getNumEventHandlers(), 0);
	}

	@Test(expectedExceptions = SelectorCreationException.class)
	public void selectorProvidersOpenSelectorException() throws Exception {
		DummySelectorProvider selectorProvider = new DummySelectorProvider();
		selectorProvider.setNextOpenSelectorException(new IOException());
		SimpleEventDispatcher.open(selectorProvider, false);
	}
}
