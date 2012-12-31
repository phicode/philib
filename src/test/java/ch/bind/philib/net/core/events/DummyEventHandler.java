package ch.bind.philib.net.core.events;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

public final class DummyEventHandler implements EventHandler {

	public final long id;

	public int closeCalls;

	public int handleOpsCalls;

	public int handleTimeoutCalls;

	public DummyEventHandler(long id) {
		this.id = id;
	}

	@Override
	public void close() throws IOException {
		closeCalls++;
	}

	@Override
	public SelectableChannel getChannel() {
		throw new AssertionError();
	}

	@Override
	public int handleOps(int ops) throws IOException {
		handleOpsCalls++;
		return 0;
	}

	@Override
	public boolean handleTimeout() throws IOException {
		handleTimeoutCalls++;
		return true;
	}

	@Override
	public long getEventHandlerId() {
		return id;
	}
}
