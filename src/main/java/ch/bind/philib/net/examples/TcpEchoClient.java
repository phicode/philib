package ch.bind.philib.net.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.net.Consumer;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.tcp.TcpConnection;

//TODO: reply data validation
//TODO: speed measurements
//TODO: many threads
public class TcpEchoClient implements Consumer {

	private byte[] buf;

	private TcpConnection connection;

	private AtomicInteger expectInput = new AtomicInteger();

	private AtomicLong counter = new AtomicLong();
	
	private long start;
	
	private long nextBlubber;

	public static void main(String[] args) throws Exception {
		new TcpEchoClient().run();
	}

	private void run() throws IOException {
		InetSocketAddress endpoint = SocketAddresses.fromIp("127.0.0.1", 1234);
		connection = TcpConnection.open(endpoint, this);

		// buf = new byte[8 * 1024];
		buf = new byte[128 * 1024];
		new Random().nextBytes(buf);

		start = System.currentTimeMillis();
		send();
	}

	private void send() throws IOException {
		// try {
		// Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		expectInput.addAndGet(buf.length);
		connection.send(buf);
		counter.addAndGet(buf.length);
		long now = System.currentTimeMillis();
		if (now > nextBlubber) {
			nextBlubber = now + 1000;
			long t = now - start;
			double mbPerSec = (counter.get() / (1024f * 1024f)) / (t / 1000f);
			System.out.printf("%d bytes in %d ms => %.3f mb/sec%n", counter.get(), t, mbPerSec);
		}
	}

	@Override
	public void receive(byte[] data) throws IOException {
		expectInput.addAndGet(-data.length);
		int missing = expectInput.get();
		if (missing < 0) {
			String msg = "server sent back more data then we sent, WTF?";
			System.out.println(msg);
			throw new Error(msg);
		} else if (missing == 0) {
			// System.out.println("server replied, sending question again");
			send();
		} else {
			System.out.println("received data, but still missing: " + missing);
		}
	}

	@Override
	public void closed() {
		System.out.println("connection closed");
	}
}
