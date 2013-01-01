package ch.bind.philib.net.core.events;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import ch.bind.philib.validation.Validation;

public class DummySelectionKey extends SelectionKey {

	private final SelectableChannel channel;

	private final Selector selector;

	public DummySelectionKey(SelectableChannel channel, Selector selector) {
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
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public int interestOps() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SelectionKey interestOps(int ops) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int readyOps() {
		// TODO Auto-generated method stub
		return 0;
	}
}
