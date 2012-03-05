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

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Consumer;
import ch.bind.philib.net.ConsumerFactory;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.tcp.TcpNetFactory;
import ch.bind.philib.validation.SimpleValidation;

public class TcpEchoServer implements ConsumerFactory {

	public static void main(String[] args) throws IOException {
		new TcpEchoServer().foo();
	}

	private void foo() throws IOException {
		InetSocketAddress bindAddress = SocketAddresses.wildcard(1234);
		ConsumerFactory consumerFactory = this;
		NetServer server = new TcpNetFactory().openServer(bindAddress, consumerFactory);
		// server.close();
	}

	@Override
	public Consumer acceptConnection(Connection connection) {
		return new EchoConsumer(connection);
	}

	private static class EchoConsumer implements Consumer {

		private final Connection connection;

		public EchoConsumer(Connection connection) {
			SimpleValidation.notNull(connection);
			this.connection = connection;
		}

		@Override
		public void receive(byte[] data) throws IOException {
			// System.out.println("received: " + data.length);
			// echo the data
			connection.send(data);
		}

		@Override
		public void closed() {
			System.out.println("closed()");
		}
	}
}
