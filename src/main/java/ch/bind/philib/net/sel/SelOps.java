package ch.bind.philib.net.sel;

import java.nio.channels.SelectionKey;

public final class SelOps {

	private SelOps() {
	}

	public static final int READ = SelectionKey.OP_READ;

	public static final int READ_WRITE = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

	public static final int ACCEPT = SelectionKey.OP_ACCEPT;
}
