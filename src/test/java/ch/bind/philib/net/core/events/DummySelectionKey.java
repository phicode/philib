package ch.bind.philib.net.core.events;

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
		this.readyOps=ops;
	}
}
