package ch.bind.philib.net.events;

public abstract class EventHandlerBase implements EventHandler {

	@Override
	public boolean handleRead(Thread thread) {
		throw new IllegalStateException("unsupported select operation: read");
	}

	@Override
	public boolean handleWrite() {
		throw new IllegalStateException("unsupported select operation: write");
	}

	@Override
	public boolean handleConnect() {
		throw new IllegalStateException("unsupported select operation: connect");
	}

	@Override
	public boolean handleAccept() {
		throw new IllegalStateException("unsupported select operation: accept");
	}
}