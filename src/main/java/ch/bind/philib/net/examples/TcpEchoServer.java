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

import ch.bind.philib.net.BaseSession;
import ch.bind.philib.net.NetServer;
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
			System.out.println("active sessions: " + server.getActiveSessionCount());
		}
	}

	@Override
	public EchoSession createSession() {
		return new EchoSession();
	}

	// TODO: make an abstract-consumer which delas with this initialization
	// stuff and prevents sending if not initialized.
	// offer a postInit() method for specific setup stuff
	private static class EchoSession extends BaseSession {

		@Override
		public void receive(byte[] data) throws IOException {
			// System.out.println("received: " + data.length);
			// echo the data
			send(data);
		}

		@Override
		public void closed() {
			System.out.println("closed()");
		}
	}
}
