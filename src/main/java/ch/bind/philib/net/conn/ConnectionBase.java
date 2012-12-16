package ch.bind.philib.net.conn;

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;

public abstract class ConnectionBase extends EventHandlerBase implements Connection {

	private final AtomicLong rx = new AtomicLong(0);

	private final AtomicLong tx = new AtomicLong(0);

	protected ConnectionBase(NetContext context) {
		super(context);
	}

	protected void incrementRx(int amount) {
		rx.addAndGet(amount);
	}

	protected void incrementTx(int amount) {
		tx.addAndGet(amount);
	}

	@Override
	public final long getRx() {
		return rx.get();
	}

	@Override
	public final long getTx() {
		return tx.get();
	}

	@Override
	public final NetContext getContext() {
		return context;
	}
}
