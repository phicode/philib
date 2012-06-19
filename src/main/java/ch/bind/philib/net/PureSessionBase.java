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
package ch.bind.philib.net;

import java.io.IOException;
import java.nio.ByteBuffer;

import ch.bind.philib.net.context.NetContext;

public abstract class PureSessionBase implements PureSession {

	private Connection connection;

	@Override
	public final void init(Connection connection) {
		this.connection = connection;
	}

	@Override
	public final NetContext getContext() {
		return connection.getContext();
	}

	@Override
	public final void close() throws IOException {
		connection.close();
	}

	@Override
	public final void send(ByteBuffer data) throws IOException {
		connection.send(data);
	}

	@Override
	public final boolean isConnected() {
		return connection.isConnected();
	}

	@Override
	public final boolean isOpen() {
		return connection.isOpen();
	}

	@Override
	public final long getRx() {
		return connection.getRx();
	}

	@Override
	public final long getTx() {
		return connection.getTx();
	}
}
