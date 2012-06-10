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

import static ch.bind.philib.io.BitOps.checkMask;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.lang.ThreadUtil;

// TODO: thread safe
public final class SimpleEventDispatcher implements EventDispatcher {

	private static final AtomicLong NAME_SEQ = new AtomicLong(0);

	private static final boolean doSelectTimings = false;

	private final Queue<NewRegistration> newRegistrations = new ConcurrentLinkedQueue<NewRegistration>();

	private final Selector selector;

	private volatile Thread thread;

	private SimpleEventDispatcher(Selector selector) throws IOException {
		this.selector = selector;
	}

	public static EventDispatcher open() throws IOException {
		Selector selector = Selector.open();
		SimpleEventDispatcher rv = new SimpleEventDispatcher(selector);
		String threadName = SimpleEventDispatcher.class.getSimpleName() + '-' + NAME_SEQ.getAndIncrement();
		rv.thread = ThreadUtil.runForever(rv, threadName);
		return rv;
	}

	@Override
	public void run() {
		int lastKeys = 0;
		try {
			while (true) {
				int num = doSelect();
				if (num > 0) {
					Set<SelectionKey> selected = selector.selectedKeys();
					for (SelectionKey key : selected) {
						handleReadyKey(thread, key);
					}
					selected.clear();
				}
				int keys = selector.keys().size();
				if (keys != lastKeys) {
					System.out.printf("keys now=%d, last=%d%n", keys, lastKeys);
					lastKeys = keys;
				}
			}
		} catch (ClosedSelectorException e) {
			System.out.println("shutting down");
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private int doSelect() throws IOException, ClosedSelectorException {
		int num;
		do {
			updateRegistrations();

			if (doSelectTimings) {
				long selStart = System.nanoTime();
				num = selector.select(10000L);
				long selEnd = System.nanoTime();
				long selTime = selEnd - selStart;
				long selMs = selTime / (10L * 1000L * 1000L);
				if (selMs >= 10005) {
					System.out.printf("select took %dms, num=%d%n", selMs, num);
				}
			} else {
				num = selector.select(10000L);
			}
		} while (num == 0);
		return num;
	}

	private void updateRegistrations() {
		NewRegistration reg = newRegistrations.poll();
		while (reg != null) {
			EventHandler selectable = reg.getSelectable();
			SelectableChannel channel = selectable.getChannel();
			int ops = reg.getOps();
			try {
				long ts = System.nanoTime();
				channel.register(selector, ops, selectable);
				long te = System.nanoTime();
				long t = te - ts;
				System.out.printf("register took: %dns => %.5fms%n", t, (t / 1000000f));
			} catch (ClosedChannelException e) {
				System.out.println("cant register an already closed channel");
			}

			reg = newRegistrations.poll();
		}
	}

	@Override
	public void close() throws IOException {
		Thread t = thread;
		if (t != null) {
			thread = null;
			ThreadUtil.interruptAndJoin(t);
			selector.close();
		}
		// TODO all registered clients as well???
		throw new UnsupportedOperationException("TODO: finish");
	}

	private void handleReadyKey(final Thread thread, final SelectionKey key) {
		EventHandler selectable = (EventHandler) key.attachment();
		if (selectable == null) {
			// canceled key
			return;
		}
		int readyOps = key.readyOps();
		boolean closed = false;
		try {
			if (checkMask(readyOps, EventUtil.READ)) {
				closed = selectable.handleRead(thread);
			}
			if (!closed && checkMask(readyOps, EventUtil.WRITE)) {
				closed = selectable.handleWrite();
			}
			if (!closed && checkMask(readyOps, EventUtil.ACCEPT)) {
				closed = selectable.handleAccept();
			}
			if (!closed && checkMask(readyOps, EventUtil.CONNECT)) {
				closed = selectable.handleConnect();
			}
		} catch (Exception e) {
			closed = true;
			System.out.println("selectable.handle*() failed: " + ExceptionUtil.buildMessageChain(e));
		}
		if (closed) {
			try {
				selectable.close();
			} catch (Exception e) {
				System.out.println("exception while closing: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void register(EventHandler selectable, int ops) {
		newRegistrations.add(new NewRegistration(selectable, ops));
		wakeup();
	}

	private void wakeup() {
		if (thread != Thread.currentThread()) {
			selector.wakeup();
		}
	}

	@Override
	public void reRegister(EventHandler selectable, int ops, boolean asap) {
		SelectableChannel channel = selectable.getChannel();
		SelectionKey key = channel.keyFor(selector);
		if (key == null) {
			System.out.println("!!!!!!!!!!!!!!! channel is not registered for this selector");
		} else {
			key.interestOps(ops);
		}
		if (asap) {
			wakeup();
		}
	}

	@Override
	public void unregister(EventHandler selectable) {
		SelectableChannel channel = selectable.getChannel();
		SelectionKey key = channel.keyFor(selector);
		if (key != null) {
			key.cancel();
			key.attach(null);
			wakeup();
			System.out.println("unreg, keys: " + selector.keys().size());
		} else {
			System.out.println("unreg failed, not registered");
		}
	}

	private static final class NewRegistration {

		final EventHandler selectable;

		final int ops;

		private NewRegistration(EventHandler selectable, int ops) {
			this.selectable = selectable;
			this.ops = ops;
		}

		public EventHandler getSelectable() {
			return selectable;
		}

		public int getOps() {
			return ops;
		}
	}
}
