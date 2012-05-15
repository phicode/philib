package ch.bind.philib.net.sel;

import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicLong;

public final class SelUtil {

	private SelUtil() {
	}

	public static final int READ = SelectionKey.OP_READ;

	public static final int WRITE = SelectionKey.OP_WRITE;

	public static final int READ_WRITE = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

	public static final int ACCEPT = SelectionKey.OP_ACCEPT;

	public static final int CONNECT = SelectionKey.OP_CONNECT;

}
