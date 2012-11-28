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
package ch.bind.philib.net.events;

import java.nio.ByteBuffer;

import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public abstract class EventHandlerBase implements EventHandler {

	protected final NetContext context;

	protected final long eventHandlerId = EventHandlerIdSeq.nextEventHandlerId();

	protected EventHandlerBase(NetContext context) {
		super();
		Validation.notNull(context);
		this.context = context;
	}

	@Override
	public final long getEventHandlerId() {
		return eventHandlerId;
	}

	protected final ByteBuffer acquireBuffer() {
		return context.getBufferCache().acquire();
	}

	protected final void freeBuffer(final ByteBuffer buf) {
		context.getBufferCache().free(buf);
	}

	protected final boolean releaseBuffer(final NetBuf buf) {
		buf.finished();
		if (buf.isIntern()) {
			context.getBufferCache().free(buf.getBuffer());
			return false;
		}
		return true;
	}
}
