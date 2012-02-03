package ch.bind.philib.net.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;

import ch.bind.philib.net.Consumer;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.tcp.TcpConnection;

//TODO: reply data validation
//TODO: speed measurements
//TODO: many threads
public class TcpEchoClient implements Consumer {

	private byte[] buf;

	private TcpConnection connection;

	private int missingInput;

	public static void main(String[] args) throws Exception {
		new TcpEchoClient().run();
	}

	private void run() throws IOException {
		InetSocketAddress endpoint = SocketAddresses.fromIp("10.0.0.65", 1234);
		connection = TcpConnection.open(endpoint, this);

		byte[] buf = new byte[16 * 1024];
		new Random().nextBytes(buf);

		send();
	}

	private void send() throws IOException {
		connection.send(buf);
		missingInput = buf.length;
	}

	@Override
	public void receive(byte[] data) throws IOException {
		missingInput -= data.length;
		if (missingInput < 0) {
			System.out.println("server sent back more data then we sent, WTF?");
			send();
		} else if (missingInput == 0) {
			System.out.println("server replied, sending question again");
			send();
		} else {
			System.out.println("received data, but still missing: " + missingInput);
		}
	}

	@Override
	public void closed() {
		System.out.println("connection closed");
	}
}
