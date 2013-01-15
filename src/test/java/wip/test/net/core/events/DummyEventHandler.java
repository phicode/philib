package wip.test.net.core.events;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import wip.src.net.core.events.EventDispatcher;
import wip.src.net.core.events.EventHandler;

import ch.bind.philib.validation.Validation;

public final class DummyEventHandler implements EventHandler {

	public final long id;

	private final SelectableChannel channel;

	private final EventDispatcher dispatcher;

	public int closeCalls;

	public int handleOpsCalls;

	public int handleTimeoutCalls;

	public volatile int handleOpsRetval;

	public volatile int lastHandleOps;

	public DummyEventHandler(long id) {
		this(id, null, null);
	}

	public DummyEventHandler(long id, SelectableChannel channel, EventDispatcher dispatcher) {
		this.id = id;
		this.channel = channel;
		this.dispatcher = dispatcher;
	}

	@Override
	public void close() throws IOException {
		closeCalls++;
		if (dispatcher != null) {
			dispatcher.unregister(this);
		}
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
		lastHandleOps = ops;
		return handleOpsRetval;
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
