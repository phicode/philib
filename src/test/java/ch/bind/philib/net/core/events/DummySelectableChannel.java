package ch.bind.philib.net.core.events;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Map;

import ch.bind.philib.lang.ServiceState;
import ch.bind.philib.validation.Validation;

public class DummySelectableChannel extends SelectableChannel {

	private final SelectorProvider selectorProvider;

	private boolean blocking;

	private final ServiceState serviceState = new ServiceState();

	private final Map<Selector, SelectionKey> keys = new HashMap<Selector, SelectionKey>();

	public DummySelectableChannel(SelectorProvider selectorProvider) {
		Validation.notNull(selectorProvider);
		this.selectorProvider = selectorProvider;
		serviceState.setOpen();
	}

	@Override
	public SelectorProvider provider() {
		return selectorProvider;
	}

	@Override
	public int validOps() {
		throw new AssertionError();
	}

	@Override
	public synchronized boolean isRegistered() {
		return !keys.isEmpty();
	}

	@Override
	public SelectionKey keyFor(Selector sel) {
		Validation.notNull(sel);
		Validation.isTrue(serviceState.isOpen());
		return keys.get(sel);
	}

	@Override
	public synchronized SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException {
		Validation.notNull(sel);
		Validation.isTrue(serviceState.isOpen());

		SelectionKey key = keys.get(sel);
		if (key == null) {
//			key = new DummySelectionKey();
			keys.put(sel, key);
		}
		key.interestOps(ops);
		key.attach(att);
		return key;
	}

	@Override
	public synchronized SelectableChannel configureBlocking(boolean block) throws IOException {
		Validation.isTrue(serviceState.isOpen());
		this.blocking = block;
		return this;
	}

	@Override
	public synchronized boolean isBlocking() {
		return blocking;
	}

	@Override
	public Object blockingLock() {
		return this;
	}

	@Override
	protected void implCloseChannel() throws IOException {
		serviceState.setClosed();
	}
}
