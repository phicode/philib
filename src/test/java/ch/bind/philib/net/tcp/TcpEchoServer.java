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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.NetListener;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.context.NetContexts;
import ch.bind.philib.net.session.EchoServerSession;
import ch.bind.philib.net.tcp.TcpServer;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class TcpEchoServer implements SessionFactory {

	public static void main(String[] args) throws Exception {
		new TcpEchoServer().foo();
	}

	private void foo() throws Exception {
		InetSocketAddress bindAddress = SocketAddresses.wildcard(1234);
		// NetContext context = new SimpleNetContext();
		NetContext context = NetContexts.createSimple(this);// new
															// ScalableNetContext(16);
		// context.setTcpNoDelay(true);
		// context.setSndBufSize(64 * 1024);
		// context.setRcvBufSize(64 * 1024);
		context.setTcpNoDelay(false);
		context.setSndBufSize(512);
		context.setRcvBufSize(512);
		NetListener server = TcpServer.open(context, bindAddress);
		System.out.println("listening on: " + bindAddress);
		while (true) {
			Thread.sleep(20000);
			synchronized (sessions) {
				if (sessions.size() > 0) {
					long now = System.nanoTime();
					long tooFarAgo = now - 10000000L; // 10ms
					Iterator<EchoServerSession> iter = sessions.iterator();
					System.out.println(server.getContext().getBufferPool().getPoolStats().toString());
					System.out.println("sessions: " + sessions.size());
					while (iter.hasNext()) {
						EchoServerSession s = iter.next();
						if (!s.getConnection().isConnected()) {
							System.out.println("removeing disconnected session: " + s);
							iter.remove();
						}
						else {
							long lastInteractionNs = s.getLastInteractionNs();
							if (lastInteractionNs < tooFarAgo) {
								double lastSec = (now - lastInteractionNs) / 1000000000f;
								System.out.printf("last interaction: %.5fsec => %s%n", //
										lastSec, s);
							}
						}
					}
				}
			}
		}
	}

	private List<EchoServerSession> sessions = new ArrayList<EchoServerSession>();

	@Override
	public synchronized EchoServerSession createSession(Connection connection) {
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