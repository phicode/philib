package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.net.SocketAddresses;

public class TcpConnection {

	private SocketChannel channel;

	void open(SocketAddress endpoint) throws IOException {
		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(true);
		channel.connect(endpoint);

		System.out.println("connected to: " + endpoint);
		this.channel = channel;
	}

	public void run() throws IOException {
		InputStream in = channel.socket().getInputStream();
		OutputStream out = channel.socket().getOutputStream();

		AtomicLong txCnt = new AtomicLong();
		AtomicLong rxCnt = new AtomicLong();

		Sender sender = new Sender(txCnt, out);
		Receiver receiver = new Receiver(txCnt, in);

		Thread tw = new Thread(sender);
		Thread tr = new Thread(receiver);
		tw.start();
		tr.start();

		while (tr.isAlive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				tw.interrupt();
				tr.interrupt();
				try {
					tw.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				try {
					tr.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			long tx = txCnt.get();
			long rx = rxCnt.get();
			System.out.println("tx=" + tx + " rx=" + rx);
		}
	}

	public static void main(String[] args) throws IOException {
		TcpConnection client = new TcpConnection();
		InetSocketAddress endpoint = SocketAddresses.fromIp("127.0.0.1", 1234);
		client.open(endpoint);
		client.run();
	}

	private static class Sender implements Runnable {
		final AtomicLong txCnt;
		final OutputStream out;

		public Sender(AtomicLong txCnt, OutputStream out) {
			super();
			this.txCnt = txCnt;
			this.out = out;
		}

		@Override
		public void run() {
			try {
				byte[] buffer = new byte[4096];
				new Random().nextBytes(buffer);
				while (true) {
					out.write(buffer);
					out.flush();
					txCnt.addAndGet(buffer.length);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class Receiver implements Runnable {
		final AtomicLong rxCnt;
		final InputStream in;

		public Receiver(AtomicLong rxCnt, InputStream in) {
			super();
			this.rxCnt = rxCnt;
			this.in = in;
		}

		@Override
		public void run() {
			try {
				byte[] buffer = new byte[4096];
				while (true) {
					int len = in.read(buffer);
					if (len == -1) {
						System.out.println("connection closed");
						return;
					} else {
						rxCnt.addAndGet(len);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int write(byte[] buf) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(buf);
		return channel.write(bb);
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(buf, off, len);
		return channel.read(bb);
	}
}
