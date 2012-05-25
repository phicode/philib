package ch.bind.philib.net.sel;

public abstract class SelectableBase implements Selectable {

	@Override
	public boolean handleRead(Thread thread) {
		throw new IllegalStateException("unsupported select operation: read");
	}

	@Override
	public boolean handleWrite(Thread thread) {
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
