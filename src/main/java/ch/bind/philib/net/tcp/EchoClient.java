package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import ch.bind.philib.net.Consumer;
import ch.bind.philib.net.SocketAddresses;

//TODO: reply data validation
//TODO: speed measurements
//TODO: many threads
public class EchoClient implements Consumer {

	private byte[] buf;

	private TcpConnection connection;

	private int missingInput;

	public static void main(String[] args) throws Exception {
		new EchoClient().run();
	}

	private void run() throws IOException {
		InetSocketAddress endpoint = SocketAddresses.fromIp("10.0.0.67", 1234);
		connection = TcpConnection.open(endpoint, this);

		byte[] buf = new byte[16 * 1024];
		new Random().nextBytes(buf);

		send();
	}

	private void send() {
		connection.send(buf);
		missingInput = buf.length;
	}

	@Override
	public void receive(byte[] data) {
		missingInput -= data.length;
		if (missingInput < 0) {
			System.out.println("server sent back more data then we sent, WTF?");
			send();
		}
		else if (missingInput == 0) {
			System.out.println("server replied, sending question again");
			send();
		}
		else {
			System.out.println("received data, but still missing: " + missingInput);
		}
	}

	@Override
	public void closed() {
		System.out.println("connection closed");
	}
}
