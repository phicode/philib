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

package ch.bind.philib.net.tcp;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.junit.Test;

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.context.SimpleNetContext;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */

// TODO: make a big file, mmap it and transfer it
public class TcpConnectionTest {

	@Test(timeout = 60000)
	public void sync() throws Exception {
		NetContext context = new SimpleNetContext();
		SocketAddress addr = SocketAddresses.localhost(1234);
		ServerFactory serverFactory = new ServerFactory();
		ClientFactory clientFactory = new ClientFactory();
		NetServer openServer = TcpNetFactory.INSTANCE.openServer(context, addr, serverFactory);
		Session openClient = TcpNetFactory.INSTANCE.openClient(context, addr, clientFactory);

		synchronized(serverFactory) {
		ServerSession server = serverFactory.session;
		}
		ClientSession client = clientFactory.session;
		assertNotNull(server);
		assertNotNull(client);
		assertTrue(context.isOpen());
		assertTrue(server.connection.isOpen());
		assertTrue(client.connection.isOpen());
		assertTrue(client.connection.isConnected());

		// this should close the server as well as the client
		context.close();

		assertFalse(context.isOpen());
		assertFalse(server.connection.isOpen());
		assertFalse(client.connection.isOpen());
		assertFalse(client.connection.isConnected());
		assertTrue(server.closedCalled);
		assertTrue(client.closedCalled);
	}

	private static final class ClientSession implements Session {

		private final Connection connection;

		private volatile boolean closedCalled;

		public ClientSession(Connection connection) {
			this.connection = connection;
		}

		@Override
		public void receive(ByteBuffer data) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void closed() {
			this.closedCalled = true;
		}

		@Override
		public void writable() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class ServerSession implements Session {

		private final Connection connection;

		private volatile boolean closedCalled;

		public ServerSession(Connection connection) {
			this.connection = connection;
		}

		@Override
		public void receive(ByteBuffer data) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void closed() {
			this.closedCalled = true;
		}

		@Override
		public void writable() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class ServerFactory implements SessionFactory {

		volatile ServerSession session;

		@Override
		public synchronized Session createSession(Connection connection) {
			assertNull(session);
			session = new ServerSession(connection);
			return session;
		}
	}

	private static final class ClientFactory implements SessionFactory {

		volatile ClientSession session;

		@Override
		public synchronized Session createSession(Connection connection) throws IOException {
			System.out.println("creating new client for: " + connection.getRemoteAddress());
			assertNull(session);
			session = new ClientSession(connection);
			return session;
		}
	}
}
