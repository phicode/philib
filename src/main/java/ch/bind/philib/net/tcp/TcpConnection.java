package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.bind.philib.io.RingBuffer;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Consumer;
import ch.bind.philib.net.NetSelector;
import ch.bind.philib.net.impl.SimpleNetSelector;
import ch.bind.philib.validation.SimpleValidation;

public class TcpConnection implements Connection {

	private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	private final SocketChannel channel;

	// private final Queue<byte[]> outQueue = new
	// ConcurrentLinkedQueue<byte[]>();

	private final RingBuffer ringBuffer = new RingBuffer();

	private Consumer consumer;

	private ByteBuffer rbuf;

	private ByteBuffer wbuf;

	private final AtomicBoolean regForWrite = new AtomicBoolean();

	private NetSelector netSelector;

	public TcpConnection(SocketChannel channel) throws IOException {
		SimpleValidation.notNull(channel);
		this.channel = channel;
	}

	void init(Consumer consumer, NetSelector netSelector) throws IOException {
		this.consumer = consumer;
		this.netSelector = netSelector;
		this.channel.configureBlocking(false);
		this.rbuf = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		this.wbuf = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		netSelector.register(this);
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
		return SelectionKey.OP_READ /* | SelectionKey.OP_CONNECT */;
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

	@Override
	public void closed() {
		// TODO Auto-generated method stub
		consumer.closed();
	}

	private void doConnect() {
		// TODO
		System.out.println("op connect");
	}

	private void doRead() {
		// TODO: implement
		try {
			rbuf.clear();
			int num = channel.read(rbuf);
			if (num == -1) {
				// TODO
				throw new UnsupportedOperationException("TODO: closed stream");
			} else {
				rbuf.flip();
				// TODO: remove
				SimpleValidation.isTrue(num == rbuf.limit());
				SimpleValidation.isTrue(num == rbuf.remaining());
				byte[] b = new byte[num];
				rbuf.get(b);
				SimpleValidation.isTrue(0 == rbuf.remaining());
//				System.out.println("read: " + b.length);
				consumer.receive(b);
			}
		} catch (IOException e) {
			// TODO: handle
			e.printStackTrace();
		}
	}
	
	@Override
	public void send(byte[] data) throws IOException {
		// TODO: handle data.length > wbuf.capacity
		wbuf.clear();
		wbuf.put(data);
		wbuf.flip();
		// TODO: remove
		SimpleValidation.isTrue(wbuf.remaining() == data.length);
		channel.write(wbuf);
		int rem = wbuf.remaining();
		if (rem > 0) {
			int off = data.length - rem;
			ringBuffer.write(data, off, rem);
			registerForWrite();
//			System.out.println("wrote: " + off + " / " + data.length + ", bufSize=" + ringBuffer.available());
		} else {
//			System.out.println("wrote: " + data.length);
		}
	}

	private void doWrite() {
		System.out.println("i am now writable, wheeee :)");
		byte[] transfer = new byte[4096];
		int toRead = Math.min(transfer.length, ringBuffer.available());
		ringBuffer.read(transfer, 0, toRead);
		try {
			send(transfer);
			if (ringBuffer.available() == 0) {
				unregisterForWrite();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void registerForWrite() {
		SimpleValidation.notNull(netSelector);
		if (regForWrite.compareAndSet(false, true)) {
			netSelector.reRegWithWrite(this);
		} else {
			System.out.println("already registered for write");
		}
	}

	private void unregisterForWrite() {
		SimpleValidation.notNull(netSelector);
		if (regForWrite.compareAndSet(true, false)) {
			netSelector.reRegWithoutWrite(this);
		} else {
			System.out.println("already unregistered from write");
		}
	}
}
