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
package ch.bind.philib.net.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.PureSessionBase;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.tcp.TcpNetFactory;

public class TcpEchoServer implements SessionFactory {

	public static void main(String[] args) throws Exception {
		new TcpEchoServer().foo();
	}

	private void foo() throws Exception {
		InetSocketAddress bindAddress = SocketAddresses.wildcard(1234);
		SessionFactory consumerFactory = this;
		NetServer server = new TcpNetFactory().openServer(bindAddress, consumerFactory);
		while (true) {
			Thread.sleep(10000);
			synchronized (sessions) {
				long now = System.nanoTime();
				long tooFarAgo = now - 5000000L; // 5ms
				Iterator<EchoSession> iter = sessions.iterator();
				while (iter.hasNext()) {
					EchoSession s = iter.next();
					if (s.closed) {
						iter.remove();
					}
					else {
						if (s.lastInteractionNs < tooFarAgo) {
							double lastSec = (now - s.lastInteractionNs) / 1000000000f;
							System.out.printf("last interaction with a session: %.5fsec%n", lastSec);
						}
						System.out.printf("data echoed: %d%n", s.numEchoed);
					}
				}
			}

			// System.out.println("sessions: " +
			// server.getActiveSessionCount());
			System.out.println("sessions in our list: " + sessions.size());
		}
	}

	private List<EchoSession> sessions = new ArrayList<EchoSession>();

	@Override
	public synchronized EchoSession createSession() {
		EchoSession session = new EchoSession();
		sessions.add(session);
		return session;
	}

	// TODO: make an abstract-consumer which delas with this initialization
	// stuff and prevents sending if not initialized.
	// offer a postInit() method for specific setup stuff
	private static class EchoSession extends PureSessionBase {

		private long lastInteractionNs;

		private boolean closed;

		private long numEchoed;

		@Override
		public void receive(ByteBuffer data) {
			try {
				int received = data.remaining();
				int num = send(data);
				numEchoed += num;
				if (num != received) {
					System.out.printf("cant echo back! only %d out of %d was sent.%n", num, received);
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			lastInteractionNs = System.nanoTime();
		}

		@Override
		public void closed() {
			this.closed = true;
			System.out.println("closed() numEchoed=" + numEchoed);
		}
	}
}
