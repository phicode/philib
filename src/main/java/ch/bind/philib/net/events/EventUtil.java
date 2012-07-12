/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.bind.philib.net.events;

import java.nio.channels.SelectionKey;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class EventUtil {

	private EventUtil() {}

	public static final int READ = SelectionKey.OP_READ;

	public static final int WRITE = SelectionKey.OP_WRITE;

	public static final int READ_WRITE = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

	public static final int ACCEPT = SelectionKey.OP_ACCEPT;

	public static final int CONNECT = SelectionKey.OP_CONNECT;

	// public static void writeAllBlocking(final Connection connection, final
	// ByteBuffer data) throws IOException {
	//
	// }
	//
	// public static void writeAllBlocking(final NetContext context, final
	// Connection connection, final byte[] data) throws IOException {
	// final ByteBuffer buf = context.getBufferCache().acquire();
	// int remaining = data.length;
	// int offset = 0;
	// while (remaining > 0) {
	// int num = copy(data, offset, buf);
	// offset += num;
	// remaining -= num;
	// writeAllBlocking(connection, buf);
	// }
	// context.getBufferCache().release(buf);
	// }
	//
	// private static int copy(final byte[] data, final int offset, final
	// ByteBuffer buf) {
	// int remaining = data.length - offset;
	// int num = Math.min(remaining, buf.capacity());
	// buf.clear();
	// buf.put(data, offset, num);
	// buf.flip();
	// return num;
	// }
}
