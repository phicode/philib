package ch.bind.philib.net.core.events;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import ch.bind.philib.validation.Validation;

public final class DummyEventHandler implements EventHandler {

	public final long id;

	private final SelectableChannel channel;

	public int closeCalls;

	public int handleOpsCalls;

	public int handleTimeoutCalls;

	public DummyEventHandler(long id) {
		this(id, null);
	}

	public DummyEventHandler(long id, SelectableChannel channel) {
		this.id = id;
		this.channel = channel;
	}

	@Override
	public void close() throws IOException {
		closeCalls++;
		if (channel != null) {
			channel.close();
		}
	}

	@Override
	public SelectableChannel getChannel() {
		Validation.notNull(channel);
		return channel;
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
