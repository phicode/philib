package ch.bind.philib.net.events;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.NetContext;

public final class EventUtil {

	private EventUtil() {
	}

	public static final int READ = SelectionKey.OP_READ;

	public static final int WRITE = SelectionKey.OP_WRITE;

	public static final int READ_WRITE = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

	public static final int ACCEPT = SelectionKey.OP_ACCEPT;

	public static final int CONNECT = SelectionKey.OP_CONNECT;

	public static void writeAllBlocking(final Connection connection, final ByteBuffer data) throws IOException {

	}

	public static void writeAllBlocking(final NetContext context, final Connection connection, final byte[] data) throws IOException {
		final ByteBuffer buf = context.getBufferCache().acquire();
		int remaining = data.length;
		int offset = 0;
		while (remaining > 0) {
			int num = copy(data, offset, buf);
			offset += num;
			remaining -= num;
			writeAllBlocking(connection, buf);
		}
		context.getBufferCache().release(buf);
	}

	private static int copy(final byte[] data, final int offset, final ByteBuffer buf) {
		int remaining = data.length - offset;
		int num = Math.min(remaining, buf.capacity());
		buf.clear();
		buf.put(data, offset, num);
		buf.flip();
		return num;
	}
}
