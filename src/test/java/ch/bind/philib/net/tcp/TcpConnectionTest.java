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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.NetListener;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionManager;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.context.NetContexts;
import ch.bind.philib.net.session.DevNullSession;
import ch.bind.philib.net.session.EchoClientSession;
import ch.bind.philib.net.session.EchoServerSession;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
@Test(singleThreaded = true)
public class TcpConnectionTest {

	private static final int MAPPED_BUFFER_SIZE = Integer.MAX_VALUE;

	private File tempFile;

	private RandomAccessFile randomAccessFile;

	private FileChannel fileChannel;

	private ByteBuffer bigMappedBuffer;

	@BeforeClass
	public void beforeClass() throws Exception {
		tempFile = File.createTempFile(getClass().getSimpleName(), ".tmp");
		randomAccessFile = new RandomAccessFile(tempFile, "rw");
		fileChannel = randomAccessFile.getChannel();

		// just a whole lot of zeros to copy around
		bigMappedBuffer = fileChannel.map(MapMode.READ_ONLY, 0, MAPPED_BUFFER_SIZE);
	}

	@AfterClass
	public void afterClass() throws Exception {
		bigMappedBuffer = null;
		SafeCloseUtil.close(fileChannel);
		fileChannel = null;
		SafeCloseUtil.close(randomAccessFile);
		randomAccessFile = null;
		// make sure that the mapped file is actually cleaned up
		int tryCount = -1;
		do {
			tryCount++;
			System.gc();
			Thread.sleep(500);

			if (tempFile != null && !tempFile.delete()) {
				fail("failed to delete: " + tempFile);
			}
		} while (tempFile != null && tempFile.exists() && tryCount < 120);
	}

	@BeforeMethod
	public void beforeMethod() {
		if (bigMappedBuffer != null) {
			bigMappedBuffer.clear();
		}
	}

	@Test(timeOut = 60000, priority = 1)
	public void connectAndDisconnect() throws Exception {
		DevNullSessionManager serverSessionManager = new DevNullSessionManager();
		DevNullSessionManager clientSessionManager = new DevNullSessionManager();
		NetContext serverContext = NetContexts.createSimple(serverSessionManager);
		NetContext clientContext = NetContexts.createSimple(clientSessionManager);
		SocketAddress addr = SocketAddresses.localhost(1234);
		NetListener netServer = TcpNetFactory.listen(serverContext, addr);
		Future<TcpConnection> clientFuture = TcpNetFactory.connect(clientContext, addr);

		TcpConnection clientConn = clientFuture.get(100, TimeUnit.MILLISECONDS);
		assertNotNull(clientConn);
		assertTrue(clientConn.isConnected());
		assertEquals(clientContext.getEventDispatcher().getNumEventHandlers(), 1);
		while (serverContext.getEventDispatcher().getNumEventHandlers() != 2) {
			Thread.yield();
		}

		DevNullSession server = serverSessionManager.session;
		DevNullSession client = clientSessionManager.session;
		assertNotNull(server);
		assertNotNull(client);
		assertTrue(client == clientConn.getSession());
		assertTrue(serverContext.isOpen());
		assertTrue(clientContext.isOpen());
		assertTrue(netServer.isOpen());
		assertTrue(server.getConnection().isOpen());
		assertTrue(client.getConnection().isOpen());
		assertTrue(client.getConnection().isConnected());
		assertTrue(server.getServiceState().isOpen());
		assertTrue(client.getServiceState().isOpen());

		serverContext.close();
		clientContext.close();

		assertFalse(serverContext.isOpen());
		assertFalse(clientContext.isOpen());
		assertFalse(netServer.isOpen());
		assertFalse(server.getConnection().isOpen());
		assertFalse(client.getConnection().isOpen());
		assertFalse(client.getConnection().isConnected());
		assertTrue(server.getServiceState().isClosed());
		assertTrue(client.getServiceState().isClosed());
	}

	@Test(timeOut = 60000, priority = 10)
	public void sendManyZeros() throws Exception {
		assertEquals(bigMappedBuffer.remaining(), MAPPED_BUFFER_SIZE);
		DevNullSessionManager serverSessionManager = new DevNullSessionManager();
		DevNullSessionManager clientSessionManager = new DevNullSessionManager();
		NetContext serverContext = NetContexts.createSimple(serverSessionManager);
		NetContext clientContext = NetContexts.createSimple(clientSessionManager);
		SocketAddress addr = SocketAddresses.localhost(1234);
		NetListener netServer = TcpNetFactory.listen(serverContext, addr);
		Future<TcpConnection> clientFuture = TcpNetFactory.connect(clientContext, addr);

		TcpConnection clientConn = clientFuture.get(100, TimeUnit.MILLISECONDS);
		assertNotNull(clientConn);
		assertTrue(clientConn.isConnected());
		assertEquals(clientContext.getEventDispatcher().getNumEventHandlers(), 1);
		while (serverContext.getEventDispatcher().getNumEventHandlers() != 2) {
			Thread.yield();
		}

		DevNullSession server = serverSessionManager.session;
		DevNullSession client = clientSessionManager.session;

		assertEquals(bigMappedBuffer.remaining(), MAPPED_BUFFER_SIZE);

		send(client.getConnection(), server.getConnection(), bigMappedBuffer);

		bigMappedBuffer.rewind();
		assertEquals(bigMappedBuffer.remaining(), MAPPED_BUFFER_SIZE);

		send(server.getConnection(), client.getConnection(), bigMappedBuffer);

		serverContext.close();
		clientContext.close();
	}

	@Test(timeOut = 60000, priority = 20)
	public void echoSomeData() throws Exception {
		final long tStart = System.currentTimeMillis();
		// ByteBufferPool pool = ByteBufferPool.create(16384, 16);
		// EventDispatcher disp = SimpleEventDispatcher.open();
		EchoServerSessionManager serverSessionManager = new EchoServerSessionManager();
		EchoClientSessionManager clientSessionManager = new EchoClientSessionManager();
		NetContext serverContext = NetContexts.createSimple(serverSessionManager);
		NetContext clientContext = NetContexts.createSimple(clientSessionManager);
		// NetContext serverContext = new NetContextImpl(serverSessionManager,
		// pool, disp);
		// NetContext clientContext = new NetContextImpl(clientSessionManager,
		// pool, disp);
		SocketAddress addr = SocketAddresses.localhost(1234);
		NetListener netServer = TcpNetFactory.listen(serverContext, addr);
		Future<TcpConnection> clientFuture = TcpNetFactory.connect(clientContext, addr);

		TcpConnection clientConn = clientFuture.get(100, TimeUnit.MILLISECONDS);
		assertNotNull(clientConn);
		assertTrue(clientConn.isConnected());
		assertEquals(clientContext.getEventDispatcher().getNumEventHandlers(), 1);
		while (serverContext.getEventDispatcher().getNumEventHandlers() != 2) {
			Thread.yield();
		}

		EchoServerSession server = serverSessionManager.session;
		EchoClientSession client = clientSessionManager.session;
		assertNotNull(server);
		assertNotNull(client);
		assertTrue(client == clientConn.getSession());
		assertTrue(serverContext.isOpen());
		assertTrue(clientContext.isOpen());
		assertTrue(netServer.isOpen());
		assertTrue(server.getConnection().isOpen());
		assertTrue(client.getConnection().isOpen());
		assertTrue(client.getConnection().isConnected());

		long time = System.currentTimeMillis() - tStart;
		while (time < 10000) {
			Thread.sleep(250);
			time = System.currentTimeMillis() - tStart;
		}

		serverContext.close();
		clientContext.close();

		assertFalse(serverContext.isOpen());
		assertFalse(clientContext.isOpen());
		assertFalse(netServer.isOpen());
		assertFalse(server.getConnection().isOpen());
		assertFalse(client.getConnection().isOpen());
		assertFalse(client.getConnection().isConnected());

		long total = client.getConnection().getTx() + client.getConnection().getRx() + server.getConnection().getTx()
				+ server.getConnection().getRx();
		System.out.printf("client tx=%d, rx=%d%n", client.getConnection().getTx(), client.getConnection().getRx());
		System.out.printf("server tx=%d, rx=%d%n", server.getConnection().getTx(), server.getConnection().getRx());
		time = System.currentTimeMillis() - tStart;
		double sec = time / 1000f;
		System.out.printf("total: %.3fMB in %.3fsec%n", (total / (1024f * 1024f)), sec);
		assertTrue(client.isVerificationOk());
	}

	private static void send(Connection from, Connection to, ByteBuffer data) throws Exception {
		long fromRx = from.getRx();
		long fromTx = from.getTx();
		long toRx = to.getRx();
		long toTx = to.getTx();

		int size = data.remaining();
		long tStart = System.nanoTime();

		while (data.hasRemaining()) {
			from.send(data);
		}

		long tEndWrite = System.nanoTime();
		assertEquals(from.getRx(), fromRx); // no change
		assertEquals(from.getTx(), fromTx + size);

		while (to.getRx() < (toRx + size)) {
			Thread.yield();
		}
		assertEquals(to.getRx(), (toRx + size));
		assertEquals(to.getTx(), toTx); // no change

		long tEndReceive = System.nanoTime();
		long t = tEndReceive - tStart;
		double mbPerSec = ((double) size) / (1024f * 1024f) / (t / 1000000000f);
		System.out.printf("send took %.3fms, send+receive took %.3fms -> %.3fMb/s%n", //
				(tEndWrite - tStart) / 1000000f, t / 1000000f, mbPerSec);
	}

	private static final class DevNullSessionManager implements SessionManager {

		volatile DevNullSession session;

		@Override
		public synchronized Session createSession(Connection connection) {
			assertNull(session);
			session = new DevNullSession(connection);
			return session;
		}

		@Override
		public void connectFailed(SocketAddress remoteAddress, Throwable cause) {
			fail(cause.getMessage());
		}
	}

	private static final class EchoClientSessionManager implements SessionManager {

		volatile EchoClientSession session;

		@Override
		public synchronized Session createSession(Connection connection) throws IOException {
			assertNull(session);
			session = new EchoClientSession(connection, true);
			return session;
		}

		@Override
		public void connectFailed(SocketAddress remoteAddress, Throwable cause) {
			fail(cause.getMessage());
		}
	}

	private static final class EchoServerSessionManager implements SessionManager {

		volatile EchoServerSession session;

		@Override
		public synchronized Session createSession(Connection connection) throws IOException {
			assertNull(session);
			session = new EchoServerSession(connection);
			return session;
		}

		@Override
		public void connectFailed(SocketAddress remoteAddress, Throwable cause) {
			fail(cause.getMessage());
		}
	}
}
