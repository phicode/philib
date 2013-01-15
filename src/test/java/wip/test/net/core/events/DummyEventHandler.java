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

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import wip.src.net.core.events.EventDispatcher;
import wip.src.net.core.events.EventHandler;
import ch.bind.philib.validation.Validation;

public final class DummyEventHandler implements EventHandler {

	public final long id;

	private final SelectableChannel channel;

	private final EventDispatcher dispatcher;

	public int closeCalls;

	public int handleOpsCalls;

	public int handleTimeoutCalls;

	public volatile int handleOpsRetval;

	public volatile int lastHandleOps;

	public DummyEventHandler(long id) {
		this(id, null, null);
	}

	public DummyEventHandler(long id, SelectableChannel channel, EventDispatcher dispatcher) {
		this.id = id;
		this.channel = channel;
		this.dispatcher = dispatcher;
	}

	@Override
	public void close() throws IOException {
		closeCalls++;
		if (dispatcher != null) {
			dispatcher.unregister(this);
		}
		if (channel != null) {
			channel.close();
		}
	}

	@Override
	public SelectableChannel getChannel() {
		Validation.notNull(channel);
		return channel;
	}

	@Override
	public int handleOps(int ops) throws IOException {
		handleOpsCalls++;
		lastHandleOps = ops;
		return handleOpsRetval;
	}

	@Override
	public boolean handleTimeout() throws IOException {
		handleTimeoutCalls++;
		return true;
	}

	@Override
	public long getEventHandlerId() {
		return id;
	}
}
