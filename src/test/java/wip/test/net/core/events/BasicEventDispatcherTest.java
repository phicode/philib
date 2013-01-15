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

package wip.test.net.core.events;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import org.testng.annotations.Test;

import wip.src.net.core.events.BasicEventDispatcher;
import wip.src.net.core.events.EventHandler;
import wip.src.net.core.events.SelectorCreationException;

public class BasicEventDispatcherTest {

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

		BasicEventDispatcher dispatcher = BasicEventDispatcher.open(selectorProvider, false);

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

		BasicEventDispatcher dispatcher = BasicEventDispatcher.open(selectorProvider, false);
		dispatcher.close();

		assertFalse(dispatcher.isOpen());

		EventHandler handler = new DummyEventHandler(0, new DummySelectableChannel(selectorProvider), dispatcher);
		dispatcher.register(handler, 0);
	}

	@Test(timeOut = 1000)
	public void registerAndClose() throws Exception {
		DummySelectorProvider selectorProvider = new DummySelectorProvider();
		DummySelector selector = new DummySelector(selectorProvider);
		selectorProvider.setNextOpenSelector(selector);

		BasicEventDispatcher dispatcher = BasicEventDispatcher.open(selectorProvider, false);

		DummySelectableChannel channel = new DummySelectableChannel(selectorProvider);
		channel.configureBlocking(false);
		DummyEventHandler handler = new DummyEventHandler(0, channel, dispatcher);
		dispatcher.register(handler, 1);

		while (!channel.isRegistered() || dispatcher.getNumEventHandlers() < 1) {
			Thread.yield();
		}
		// assertTrue(channel.isRegistered());
		assertEquals(dispatcher.getNumEventHandlers(), 1);
		assertEquals(dispatcher.getRegisteredOps(handler), 1);
		assertTrue(channel.isRegistered());
		assertTrue(channel.isOpen());

		dispatcher.close();

		assertFalse(dispatcher.isOpen());
		assertEquals(handler.closeCalls, 1);
		assertEquals(dispatcher.getNumEventHandlers(), 0);
		assertFalse(channel.isRegistered());
		assertFalse(channel.isOpen());
	}

	@Test(timeOut = 1000)
	public void registerEventUnregisterClose() throws Exception {
		DummySelectorProvider selectorProvider = new DummySelectorProvider();
		DummySelector selector = new DummySelector(selectorProvider);
		selectorProvider.setNextOpenSelector(selector);

		BasicEventDispatcher dispatcher = BasicEventDispatcher.open(selectorProvider, false);

		DummySelectableChannel channel = new DummySelectableChannel(selectorProvider);
		channel.configureBlocking(false);
		DummyEventHandler handler = new DummyEventHandler(0, channel, dispatcher);
		dispatcher.register(handler, 1);

		while (!channel.isRegistered() || dispatcher.getNumEventHandlers() < 1) {
			Thread.yield();
		}
		assertEquals(dispatcher.getNumEventHandlers(), 1);
		assertEquals(dispatcher.getRegisteredOps(handler), 1);
		SelectionKey k = channel.keyFor(selector);
		assertNotNull(k);
		DummySelectionKey key = (DummySelectionKey) k;

		key.setReadyOps(3);
		handler.handleOpsRetval = 2;

		assertEquals(handler.handleOpsCalls, 0);
		selector.addReadySelectionKey(channel.keyFor(selector));
		while (handler.handleOpsCalls < 1) {
			Thread.yield();
		}
		assertEquals(handler.handleOpsCalls, 1);
		assertEquals(handler.lastHandleOps, 3);
		assertEquals(dispatcher.getRegisteredOps(handler), 2);

		handler.close();

		while (channel.isRegistered()) {
			Thread.yield();
		}

		dispatcher.close();

		assertFalse(dispatcher.isOpen());
		assertEquals(dispatcher.getNumEventHandlers(), 0);
		assertFalse(channel.isRegistered());
		assertFalse(channel.isOpen());
	}

	@Test(expectedExceptions = SelectorCreationException.class)
	public void selectorProvidersOpenSelectorException() throws Exception {
		DummySelectorProvider selectorProvider = new DummySelectorProvider();
		selectorProvider.setNextOpenSelectorException(new IOException());
		BasicEventDispatcher.open(selectorProvider, false);
	}
}
