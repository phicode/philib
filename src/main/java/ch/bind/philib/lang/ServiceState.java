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

package ch.bind.philib.lang;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class ServiceState {

	private static final int STATE_UNINITIALIZED = 0;

	private static final int STATE_OPEN = 1;

	private static final int STATE_CLOSING = 2;

	private static final int STATE_CLOSED = 3;

	private AtomicInteger state = new AtomicInteger(STATE_UNINITIALIZED);

	public boolean isUninitialized() {
		return state.get() == STATE_UNINITIALIZED;
	}

	public boolean isOpen() {
		return state.get() == STATE_OPEN;
	}

	public boolean isClosing() {
		return state.get() == STATE_CLOSING;
	}

	public boolean isClosed() {
		return state.get() == STATE_CLOSED;
	}

	public boolean isClosingOrClosed() {
		return state.get() >= STATE_CLOSING;
	}

	public void setOpen() {
		switchState(STATE_OPEN);
	}

	public void setClosing() {
		switchState(STATE_CLOSING);
	}

	public void setClosed() {
		switchState(STATE_CLOSED);
	}

	private void switchState(int newState) {
		while (true) {
			int stateNow = state.get();
			if (newState < stateNow) {
				throw new IllegalStateException("service-states can only be moved forward");
			}
			if (state.compareAndSet(stateNow, newState)) {
				return;
			}
		}
	}
}
