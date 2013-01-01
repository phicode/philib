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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.atomic.AtomicReference;

public final class DummySelectorProvider extends SelectorProvider {

	private final AtomicReference<AbstractSelector> nextOpenSelector = new AtomicReference<AbstractSelector>();
	
	private final AtomicReference<IOException> nextOpenSelectorException = new AtomicReference<IOException>();

	public void setNextOpenSelector(AbstractSelector selector) {
		assertNotNull(selector);
		assertTrue(nextOpenSelector.compareAndSet(null, selector));
	}

	public void setNextOpenSelectorException(IOException exc) {
		assertNotNull(exc);
		assertTrue(nextOpenSelectorException.compareAndSet(null, exc));
	}

	@Override
	public DatagramChannel openDatagramChannel() throws IOException {
		throw new AssertionError();
	}

	@Override
	public Pipe openPipe() throws IOException {
		throw new AssertionError();
	}

	@Override
	public AbstractSelector openSelector() throws IOException {
		IOException exc = nextOpenSelectorException.getAndSet(null);
		if (exc != null) {
			throw exc;
		}
		AbstractSelector selector = nextOpenSelector.getAndSet(null);
		assertNotNull(selector);
		return selector;
	}

	@Override
	public ServerSocketChannel openServerSocketChannel() throws IOException {
		throw new AssertionError();
	}

	@Override
	public SocketChannel openSocketChannel() throws IOException {
		throw new AssertionError();
	}

	@Override
	public Channel inheritedChannel() throws IOException {
		throw new AssertionError();
	}
}
