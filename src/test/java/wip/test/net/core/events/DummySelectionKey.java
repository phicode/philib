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

package wip.test.net.core.events;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;

import ch.bind.philib.validation.Validation;

public class DummySelectionKey extends AbstractSelectionKey {

	private final SelectableChannel channel;

	private final DummySelector selector;

	private int interestedOps;

	private int readyOps;

	public DummySelectionKey(SelectableChannel channel, DummySelector selector) {
		Validation.notNull(channel);
		Validation.notNull(selector);
		this.channel = channel;
		this.selector = selector;
	}

	@Override
	public SelectableChannel channel() {
		return channel;
	}

	@Override
	public Selector selector() {
		return selector;
	}

	@Override
	public synchronized int interestOps() {
		return interestedOps;
	}

	@Override
	public synchronized SelectionKey interestOps(int ops) {
		this.interestedOps = ops;
		return this;
	}

	@Override
	public synchronized int readyOps() {
		return readyOps;
	}

	public synchronized void setReadyOps(int ops) {
		this.readyOps = ops;
	}
}
