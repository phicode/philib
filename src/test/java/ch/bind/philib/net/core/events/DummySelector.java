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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.bind.philib.lang.ServiceState;

public final class DummySelector extends AbstractSelector {

	private boolean doWakeup = false;

	private ServiceState state = new ServiceState();

	private Map<AbstractSelectableChannel, AbstractSelectionKey> keys = new HashMap<AbstractSelectableChannel, AbstractSelectionKey>();

	private Set<SelectionKey> selectedKeys = new HashSet<SelectionKey>();

	public volatile int registerCalls;

	protected DummySelector(SelectorProvider provider) {
		super(provider);
	}

	@Override
	protected void implCloseSelector() throws IOException {
		try {
			for (AbstractSelectionKey key : keys.values()) {
				super.deregister(key);
			}
			keys.clear();
			selectedKeys.clear();
			state.setClosed();
		} catch (IllegalStateException e) {
			throw new IOException("implCloseSelector failed", e);
		}
	}

	@Override
	protected synchronized SelectionKey register(AbstractSelectableChannel ch, int ops, Object att) {
		registerCalls++;
		AbstractSelectionKey key = keys.get(ch);
		if (key != null) {
			return key;
		}
		key = new DummySelectionKey(ch, this);
		key.interestOps(ops);
		key.attach(att);
		keys.put(ch, key);
		return key;
	}

	@Override
	public synchronized Set<SelectionKey> keys() {
		return new HashSet<SelectionKey>(keys.values());
	}

	@Override
	public Set<SelectionKey> selectedKeys() {
		return selectedKeys;
	}

	@Override
	public synchronized int selectNow() throws IOException {
		doWakeup = false;
		return selectedKeys.size();
	}

	@Override
	public synchronized int select(long timeout) throws IOException {
		begin();
		try {
			long now = System.currentTimeMillis();
			long until = now + timeout;
			while (!doWakeup && super.cancelledKeys().isEmpty() && selectedKeys.isEmpty() && now < until) {
				long remaining = until - now;
				try {
					wait(remaining);
				} catch (InterruptedException e) {
					throw new InterruptedIOException(e.getMessage());
				}
				now = System.currentTimeMillis();
			}
			doWakeup = false;
			return selectedKeys.size();
		} finally {
			end();
		}
	}

	@Override
	public int select() throws IOException {
		return select(0);
	}

	@Override
	public synchronized Selector wakeup() {
		doWakeup = true;
		notifyAll();
		return this;
	}

	public synchronized void addReadySelectionKey(SelectionKey selectionKey) {
		selectedKeys.add(selectionKey);
		notifyAll();
	}
}
