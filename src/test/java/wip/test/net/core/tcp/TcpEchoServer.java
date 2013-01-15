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
package wip.test.net.core.tcp;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wip.src.net.core.Connection;
import wip.src.net.core.NetListener;
import wip.src.net.core.SessionManager;
import wip.src.net.core.SocketAddresses;
import wip.src.net.core.context.NetContext;
import wip.src.net.core.context.NetContextImpl;
import wip.src.net.core.events.ConcurrentEventDispatcher;
import wip.src.net.core.events.EventDispatcher;
import wip.src.net.core.session.EchoServerSession;
import wip.src.net.core.tcp.TcpNetFactory;

import ch.bind.philib.pool.buffer.ByteBufferPool;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class TcpEchoServer implements SessionManager {

	public static void main(String[] args) throws Exception {
		new TcpEchoServer().foo();
	}

	private void foo() throws Exception {
		InetSocketAddress bindAddress = SocketAddresses.wildcard(1234);
		// NetContext context = new SimpleNetContext();

		ByteBufferPool bufferPool = ByteBufferPool.create(8192, 128, 2);
		EventDispatcher eventDispatcher = ConcurrentEventDispatcher.open(2);
		NetContext context = new NetContextImpl(this, bufferPool, eventDispatcher);
		// NetContext context = NetContexts.createSimple(this);// new
		// ScalableNetContext(16);
		// context.setTcpNoDelay(true);
		context.setSndBufSize(64 * 1024);
		context.setRcvBufSize(64 * 1024);

		// context.setSndBufSize(1024 * 1024);
		// context.setRcvBufSize(1024 * 1024);

		// context.setTcpNoDelay(false);
		// context.setSndBufSize(512);
		// context.setRcvBufSize(512);
		NetListener server = TcpNetFactory.listen(context, bindAddress);
		System.out.println("listening on: " + bindAddress);
		while (true) {
			Thread.sleep(20000);
			synchronized (sessions) {
				if (sessions.size() > 0) {
					long now = System.nanoTime();
					long tooFarAgo = now - (25 * 1000000L); // 25ms
					Iterator<EchoServerSession> iter = sessions.iterator();
					while (iter.hasNext()) {
						EchoServerSession s = iter.next();
						if (!s.getConnection().isConnected()) {
							System.out.println("removeing disconnected session: " + s);
							iter.remove();
						} else {
							long lastInteractionNs = s.getLastInteractionNs();
							if (lastInteractionNs < tooFarAgo) {
								double lastSec = (now - lastInteractionNs) / 1000000000f;
								System.out.printf("last interaction: %.5fsec => %s%n", //
										lastSec, s);
							}

						}
					}
					System.out.println(server.getContext().getBufferPool().getPoolStats().toString());
					System.out.println("sessions: " + sessions.size());
					System.out.println("#handlers: " + context.getEventDispatcher().getNumEventHandlers());
				}
			}
		}
	}

	private List<EchoServerSession> sessions = new ArrayList<EchoServerSession>();

	@Override
	public EchoServerSession createSession(Connection connection) {
		EchoServerSession session = new EchoServerSession(connection);
		synchronized (sessions) {
			sessions.add(session);
		}
		return session;
	}

	@Override
	public void connectFailed(SocketAddress remoteAddress, Throwable cause) {
		// TODO Auto-generated method stub
		System.out.println("should never be called on the server side");
	}
}
