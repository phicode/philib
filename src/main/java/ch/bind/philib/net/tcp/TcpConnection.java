package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Consumer;
import ch.bind.philib.net.NetSelector;
import ch.bind.philib.net.impl.SimpleNetSelector;
import ch.bind.philib.validation.SimpleValidation;

public class TcpConnection implements Connection {

	private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	private final SocketChannel channel;

	private Consumer consumer;

	private ByteBuffer rbuf;
	private ByteBuffer wbuf;

	private boolean writeReady;

	public TcpConnection(SocketChannel channel) throws IOException {
		SimpleValidation.notNull(channel);
		this.channel = channel;
	}

	void init(Consumer consumer, NetSelector selector) throws IOException {
		this.consumer = consumer;
		this.channel.configureBlocking(false);
		this.rbuf = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		this.wbuf = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		selector.register(this);
	}

	public static TcpConnection open(SocketAddress endpoint, Consumer consumer) throws IOException {
		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(true);
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}

		System.out.println("connected to: " + endpoint);
		TcpConnection con = new TcpConnection(channel);
		// TODO: selector through params
		con.init(consumer, SimpleNetSelector.open());
		return con;
	}

	// public void run() throws IOException {
	// InputStream in = channel.socket().getInputStream();
	// OutputStream out = channel.socket().getOutputStream();
	//
	// AtomicLong txCnt = new AtomicLong();
	// AtomicLong rxCnt = new AtomicLong();
	//
	// Sender sender = new Sender(txCnt, out);
	// Receiver receiver = new Receiver(txCnt, in);
	//
	// Thread tw = new Thread(sender);
	// Thread tr = new Thread(receiver);
	// tw.start();
	// tr.start();
	//
	// while (tr.isAlive()) {
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// tw.interrupt();
	// tr.interrupt();
	// try {
	// tw.join();
	// } catch (InterruptedException e1) {
	// e1.printStackTrace();
	// }
	// try {
	// tr.join();
	// } catch (InterruptedException e1) {
	// e1.printStackTrace();
	// }
	// }
	// long tx = txCnt.get();
	// long rx = rxCnt.get();
	// System.out.println("tx=" + tx + " rx=" + rx);
	// }
	// }

	// public static void main(String[] args) throws IOException {
	// TcpConnection client = new TcpConnection();
	// InetSocketAddress endpoint = SocketAddresses.fromIp("127.0.0.1", 1234);
	// client.open(endpoint);
	// client.run();
	// }

	// private static class Sender implements Runnable {
	// final AtomicLong txCnt;
	// final OutputStream out;
	//
	// public Sender(AtomicLong txCnt, OutputStream out) {
	// super();
	// this.txCnt = txCnt;
	// this.out = out;
	// }
	//
	// @Override
	// public void run() {
	// try {
	// byte[] buffer = new byte[4096];
	// new Random().nextBytes(buffer);
	// while (true) {
	// out.write(buffer);
	// out.flush();
	// txCnt.addAndGet(buffer.length);
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }

	// private static class Receiver implements Runnable {
	// final AtomicLong rxCnt;
	// final InputStream in;
	//
	// public Receiver(AtomicLong rxCnt, InputStream in) {
	// super();
	// this.rxCnt = rxCnt;
	// this.in = in;
	// }
	//
	// @Override
	// public void run() {
	// try {
	// byte[] buffer = new byte[4096];
	// while (true) {
	// int len = in.read(buffer);
	// if (len == -1) {
	// System.out.println("connection closed");
	// return;
	// } else {
	// rxCnt.addAndGet(len);
	// }
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }

	// public int write(byte[] buf) throws IOException {
	// ByteBuffer bb = ByteBuffer.wrap(buf);
	// return channel.write(bb);
	// }
	//
	// public int read(byte[] buf, int off, int len) throws IOException {
	// ByteBuffer bb = ByteBuffer.wrap(buf, off, len);
	// return channel.read(bb);
	// }

	@Override
	public void send(byte[] data) throws IOException {
		wbuf.clear();
		wbuf.put(data);
		wbuf.flip();
		channel.write(wbuf);
		// // TODO Auto-generated method stub
		// throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public int getSelectorOps() {
		return SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT;
	}

	@Override
	public void handle(int selectOp) {
		if (selectOp == SelectionKey.OP_CONNECT) {
			doConnect();
		} else if (selectOp == SelectionKey.OP_READ) {
			doRead();
		} else if (selectOp == SelectionKey.OP_WRITE) {
			doWrite();
		} else {
			throw new IllegalArgumentException("illegal select-op");
		}
	}

	private void doConnect() {
		// TODO
		System.out.println("op connect");
	}

	private void doRead() {
		// TODO: implement
		try {
			int num = channel.read(rbuf);
			if (num == -1) {
				// TODO
				throw new UnsupportedOperationException("TODO: closed stream");
			} else {
				rbuf.flip();
				byte[] b = new byte[rbuf.limit()];
				rbuf.get(b);
				consumer.receive(b);
			}
		} catch (IOException e) {
			// TODO: handle
			e.printStackTrace();
		}
	}

	private void doWrite() {
		System.out.println("i am now writable, wheeee :)");
		// TODO Auto-generated method stub
		writeReady = true;
	}
}
