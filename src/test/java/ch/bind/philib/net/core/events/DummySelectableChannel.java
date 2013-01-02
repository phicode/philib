package ch.bind.philib.net.core.events;

import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public class DummySelectableChannel extends AbstractSelectableChannel {

	public DummySelectableChannel(SelectorProvider selectorProvider) {
		super(selectorProvider);
	}

	@Override
	public int validOps() {
		return 0xFF;
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
	}

	@Override
	protected void implConfigureBlocking(boolean block) throws IOException {
	}
}
