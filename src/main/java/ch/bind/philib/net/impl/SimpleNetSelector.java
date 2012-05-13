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
package ch.bind.philib.net.impl;

import static ch.bind.philib.io.BitOps.checkMask;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.lang.ThreadUtil;
import ch.bind.philib.net.sel.NetSelector;
import ch.bind.philib.net.sel.Selectable;
import ch.bind.philib.validation.SimpleValidation;

// TODO: thread safe
public final class SimpleNetSelector implements NetSelector {

	private static final AtomicLong NAME_SEQ = new AtomicLong(0);

	private final boolean useSelectorWakeup = false;

	private final Selector selector;

	private volatile Thread thread;

	private SimpleNetSelector() throws IOException {
		selector = Selector.open();
	}

	public static NetSelector open() throws IOException {
		NetSelector rv = new SimpleNetSelector();
		String threadName = SimpleNetSelector.class.getSimpleName() + '-' + NAME_SEQ.getAndIncrement();
		new Thread(rv, threadName).start();
		return rv;
	}

	@Override
	public void run() {
		thread = Thread.currentThread();
		int lastKeys = 0;
		try {
			// TODO: wait as long as there is no channel registered
			while (true) {
				// System.out.println("asdfasdfasdfadf");
				int num = doSelect();
				if (num > 0) {
					Set<SelectionKey> selected = selector.selectedKeys();
					for (SelectionKey key : selected) {
						handleReadyKey(key);
					}
					selected.clear();
				}
				int keys = selector.keys().size();
				if (keys != lastKeys) {
					System.out.printf("keys now=%d, last=%d%n", keys, lastKeys);
					lastKeys = keys;
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private int doSelect() throws IOException {
		long selStart = System.nanoTime();
		int num = selector.select(1000L);
		long selEnd = System.nanoTime();
		long selTime = selEnd - selStart;
		long selMs = selTime / (1000L * 1000L);
		if (selMs <= 5 || selMs >= 1005) {
			System.out.printf("select took %dms, num=%d%n", selMs, num);
		}
		return num;
	}

	@Override
	public void close() throws IOException {
		ThreadUtil.interruptAndJoin(thread);
		selector.close();
		throw new UnsupportedOperationException("TODO: finish");
	}

	private void handleReadyKey(SelectionKey key) {
		Selectable selectable = (Selectable) key.attachment();
		if (selectable == null) {
			// canceled key
			return;
		}
		int readyOps = key.readyOps();
		boolean closed = false;
		try {
			if (checkMask(readyOps, SelectionKey.OP_READ)) {
				// System.out.println("OP_READ");
				closed = selectable.handle(SelectionKey.OP_READ);
			}
			if (checkMask(readyOps, SelectionKey.OP_WRITE)) {
				// System.out.println("OP_WRITE");
				closed = selectable.handle(SelectionKey.OP_WRITE);
			}
			if (checkMask(readyOps, SelectionKey.OP_ACCEPT)) {
				// System.out.println("OP_ACCEPT");
				closed = selectable.handle(SelectionKey.OP_ACCEPT);
			}
			if (checkMask(readyOps, SelectionKey.OP_CONNECT)) {
				// System.out.println("OP_CONNECT");
				closed = selectable.handle(SelectionKey.OP_CONNECT);
			}
		} catch (Exception e) {
			closed = true;
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			e.printStackTrace();
		}
		if (closed) {
			unregister(selectable);
			selectable.closed();
		}
	}

	@Override
	public void register(Selectable selectable, int ops) {
		try {
			SelectableChannel channel = selectable.getChannel();
			if ((ops & SelectionKey.OP_WRITE) != 0) {
				throw new IllegalArgumentException("SelectionKey.OP_WRITE is set in the default set");
			}
			SelectionKey key = channel.register(selector, ops, selectable);
			if (useSelectorWakeup) {
				selector.wakeup();
			}
			System.out.println("reg, keys: " + selector.keys().size());
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reRegister(Selectable selectable, int ops) {
		try {
			SelectableChannel channel = selectable.getChannel();
			SelectionKey key = channel.register(selector, ops, selectable);
			if (useSelectorWakeup) {
				selector.wakeup();
			}
			// TODO: remove
			SimpleValidation.isTrue(key.interestOps() == ops);
			System.out.println("re-reg, keys: " + selector.keys().size());
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unregister(Selectable selectable) {
		SelectableChannel channel = selectable.getChannel();
		SelectionKey key = channel.keyFor(selector);
		if (key != null) {
			key.cancel();
			key.attach(null);
			if (useSelectorWakeup) {
				selector.wakeup();
			}
			System.out.println("unreg, keys: " + selector.keys().size());
		}
		else {
			System.out.println("unreg failed, not registered");
		}
	}
}
