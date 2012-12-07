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
package ch.bind.philib.net.session;

import java.io.IOException;
import java.nio.ByteBuffer;

import ch.bind.philib.io.BufferUtil;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Events;
import ch.bind.philib.net.Session;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class EchoServerSession implements Session {

	private volatile long lastInteractionNs;

	private final Connection connection;

	private ByteBuffer backlog;

	public EchoServerSession(Connection connection) {
		this.connection = connection;
		backlog = connection.getContext().getBufferPool().take();
		backlog.limit(0);
	}

	@Override
	public Events receive(Connection conn, ByteBuffer data) throws IOException {
		lastInteractionNs = System.nanoTime();
		if (backlog.hasRemaining()) {
			conn.send(backlog);
		}
		if (!backlog.hasRemaining()) {
			conn.send(data);
		}
		if (data.hasRemaining()) {
			backlog = BufferUtil.append(backlog, data);
			// disable receiving more data until we have written the backlog
			return Events.SENDABLE;
		}
		return Events.RECEIVE;
	}

	@Override
	public Events sendable(Connection conn) throws IOException {
		lastInteractionNs = System.nanoTime();
		if (backlog.hasRemaining()) {
			conn.send(backlog);
		}
		if (backlog.hasRemaining()) {
			return Events.SENDABLE;
		}
		return Events.RECEIVE;
	}

	@Override
	public void closed(Connection conn) {
		connection.getContext().getBufferPool().recycle(backlog);
	}

	public Connection getConnection() {
		return connection;
	}

	public long getLastInteractionNs() {
		return lastInteractionNs;
	}

	@Override
	public String toString() {
		return String.format("%s[rx=%d, tx=%d, remote=%s]", getClass().getSimpleName(), connection.getRx(), connection.getTx(),
				connection.getRemoteAddress());
	}
}
