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

package ch.bind.philib.net.conn;

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;

public abstract class ConnectionBase extends EventHandlerBase implements Connection {

	private final AtomicLong rx = new AtomicLong(0);

	private final AtomicLong tx = new AtomicLong(0);

	protected ConnectionBase(NetContext context) {
		super(context);
	}

	protected void incrementRx(int amount) {
		rx.addAndGet(amount);
	}

	protected void incrementTx(int amount) {
		tx.addAndGet(amount);
	}

	@Override
	public final long getRx() {
		return rx.get();
	}

	@Override
	public final long getTx() {
		return tx.get();
	}

	@Override
	public final NetContext getContext() {
		return context;
	}

	@Override
	public final void setTimeout(long timeout) {
		context.getEventDispatcher().setTimeout(this, timeout);
	}

	@Override
	public final void unsetTimeout() {
		context.getEventDispatcher().unsetTimeout(this);
	}
}
