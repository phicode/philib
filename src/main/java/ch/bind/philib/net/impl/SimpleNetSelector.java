package ch.bind.philib.net.impl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.lang.ThreadUtil;
import ch.bind.philib.net.NetSelector;
import ch.bind.philib.net.Selectable;

// TODO: thread safe
public class SimpleNetSelector implements NetSelector {

	private static final AtomicLong NAME_SEQ = new AtomicLong(0);

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
		try {
			// TODO: wait as long as there is no channel registered
			while (true) {
				System.out.println("asdfasdfasdfadf");
				int num = selector.select(1000L);
				if (num > 0) {
					Set<SelectionKey> selected = selector.selectedKeys();
					for (SelectionKey key : selected) {
						handleReadyKey(key);
					}
					selected.clear();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		ThreadUtil.interruptAndJoin(thread);
		selector.close();
		throw new UnsupportedOperationException("TODO");
	}

	private void handleReadyKey(SelectionKey key) {
		Selectable selectable = (Selectable) key.attachment();
		if (selectable == null) {
			// canceled key
			return;
		}
		int readyOps = key.readyOps();
		if (checkMask(readyOps, SelectionKey.OP_ACCEPT)) {
			selectable.handle(SelectionKey.OP_ACCEPT);
		}
		if (checkMask(readyOps, SelectionKey.OP_CONNECT)) {
			selectable.handle(SelectionKey.OP_CONNECT);
		}
		if (checkMask(readyOps, SelectionKey.OP_READ)) {
			selectable.handle(SelectionKey.OP_READ);
		}
		if (checkMask(readyOps, SelectionKey.OP_WRITE)) {
			selectable.handle(SelectionKey.OP_WRITE);
		}
	}

	private boolean checkMask(int field, int mask) {
		return (field & mask) == mask;
	}

	public void register(Selectable selectable) {
		try {
			SelectableChannel channel = selectable.getChannel();
			int ops = selectable.getSelectorOps();

			SelectionKey key = channel.register(selector, ops);
			key.attach(selectable);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
	}

	public void unregister(Selectable selectable) {
		SelectableChannel channel = selectable.getChannel();
		SelectionKey key = channel.keyFor(selector);
		key.cancel();
		key.attach(null);
	}
}
