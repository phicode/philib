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

import ch.bind.philib.lang.ServiceState;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Session;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */

public class DevNullSession implements Session {

	private final Connection connection;

	private final ServiceState serviceState = new ServiceState();

	public DevNullSession(Connection connection) {
		this.connection = connection;
		serviceState.setOpen();
	}

	@Override
	public void receive(Connection conn, ByteBuffer data) throws IOException {
		// "consume" all data
		// data.position(data.limit());
	}

	@Override
	public void closed(Connection conn) {
		serviceState.setClosed();
	}

	@Override
	public void writable(Connection conn) {
		// there will never be anything to write
	}

	public Connection getConnection() {
		return connection;
	}

	public ServiceState getServiceState() {
		return serviceState;
	}
}
