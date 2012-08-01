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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.context.SimpleNetContext;
import ch.bind.philib.net.session.DevNullSession;

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
				System.err.println("failed to delete: " + tempFile);
			}
		} while (tempFile.exists() && tryCount < 120);
	}

	@BeforeMethod
	public void beforeMethod() {
		if (bigMappedBuffer != null) {
			bigMappedBuffer.clear();
		}
	}

	@Test(timeOut = 60000, priority = 0)
	public void connectAndDisconnect() throws Exception {
		assertEquals(bigMappedBuffer.remaining(), MAPPED_BUFFER_SIZE);
		NetContext context = new SimpleNetContext();
		SocketAddress addr = SocketAddresses.localhost(1234);
		DevNullSessionFactory serverSessionFactory = new DevNullSessionFactory();
		DevNullSessionFactory clientSessionFactory = new DevNullSessionFactory();
		NetServer netServer = TcpNetFactory.INSTANCE.openServer(context, addr, serverSessionFactory);
		Session clientS = TcpNetFactory.INSTANCE.syncOpenClient(context, addr, clientSessionFactory);

		// give some time for the client and server-side of the connection to establish proper fusion power
		Thread.sleep(50);

		DevNullSession server = serverSessionFactory.session;
		DevNullSession client = clientSessionFactory.session;
		assertNotNull(server);
		assertNotNull(client);
		assertTrue(client == clientS);
		assertTrue(context.isOpen());
		assertTrue(netServer.isOpen());
		assertTrue(server.getConnection().isOpen());
		assertTrue(client.getConnection().isOpen());
		assertTrue(client.getConnection().isConnected());
		assertTrue(server.getServiceState().isOpen());
		assertTrue(client.getServiceState().isOpen());

		// this should close the server as well as the client
		context.close();

		assertFalse(context.isOpen());
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
		NetContext context = new SimpleNetContext();
		SocketAddress addr = SocketAddresses.localhost(1234);
		DevNullSessionFactory serverSessionFactory = new DevNullSessionFactory();
		DevNullSessionFactory clientSessionFactory = new DevNullSessionFactory();
		NetServer netServer = TcpNetFactory.INSTANCE.openServer(context, addr, serverSessionFactory);
		Session clientS = TcpNetFactory.INSTANCE.syncOpenClient(context, addr, clientSessionFactory);

		// give some time for the client and server-side of the connection to establish proper fusion power
		Thread.sleep(50);

		DevNullSession server = serverSessionFactory.session;
		DevNullSession client = clientSessionFactory.session;

		assertEquals(bigMappedBuffer.remaining(), MAPPED_BUFFER_SIZE);

		sendSync(client.getConnection(), server.getConnection(), bigMappedBuffer);

		bigMappedBuffer.rewind();
		assertEquals(bigMappedBuffer.remaining(), MAPPED_BUFFER_SIZE);

		sendSync(server.getConnection(), client.getConnection(), bigMappedBuffer);

		context.close();
	}

	private static void sendSync(Connection from, Connection to, ByteBuffer data) throws Exception {
		long fromRx = from.getRx();
		long fromTx = from.getTx();
		long toRx = to.getRx();
		long toTx = to.getTx();

		int size = data.remaining();
		long tStart = System.nanoTime();
		from.sendSync(data);
		assertEquals(data.remaining(), 0);

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
		System.out.printf("write took %.3fms, write+receive took %.3fms -> %.3fMb/s%n", //
				(tEndWrite - tStart) / 1000000f, t / 1000000f, mbPerSec);
	}

	private static final class DevNullSessionFactory implements SessionFactory {

		volatile DevNullSession session;

		@Override
		public synchronized Session createSession(Connection connection) {
			assertNull(session);
			session = new DevNullSession(connection);
			return session;
		}
	}
}
