package ch.bind.philib.net;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// TODO: thread safe
public class NetSelector implements Runnable {

	private List<SelectableChannel> channels = new LinkedList<SelectableChannel>();
	private Selector selector;

	public static NetSelector open() {
		NetSelector sel = new NetSelector();
		Thread t = new Thread(sel);
		t.start();
		return sel;
	}

	@Override
	public void run() {
		try {
			selector = Selector.open();
			// TODO: wait while there is no channel registered
			while (true) {
				int num = selector.select();
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

	public void unregister(NetServer server) {
		SelectionKey key = server.getChannel().keyFor(selector);
		key.cancel();
		key.attach(null);
	}
}
