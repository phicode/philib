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
