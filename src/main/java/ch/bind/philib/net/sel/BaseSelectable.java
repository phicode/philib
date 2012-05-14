package ch.bind.philib.net.sel;

public abstract class BaseSelectable implements Selectable {

	@Override
	public boolean handleRead() {
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
