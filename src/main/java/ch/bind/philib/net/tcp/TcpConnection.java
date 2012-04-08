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
package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.bind.philib.io.NQueue;
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

//	private final RingBuffer ringBuffer = new RingBuffer();

	// private Consumer consumer;

	private ByteBuffer rbuf;

	private ByteBuffer wbuf;

	private final AtomicBoolean regForWrite = new AtomicBoolean();

	private NetSelector netSelector;

	private final NQueue<byte[]> inbox;

	public TcpConnection(SocketChannel channel, Semaphore receiveSem) throws IOException {
		SimpleValidation.notNull(channel);
		this.channel = channel;
		this.inbox = new NQueue<byte[]>(receiveSem);
	}

	void init(NetSelector netSelector) throws IOException {
		// this.consumer = consumer;
		this.netSelector = netSelector;
		this.channel.configureBlocking(false);
		this.rbuf = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		this.wbuf = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		netSelector.register(this);
	}

	public static TcpConnection open(SocketAddress endpoint, Semaphore receiveSem) throws IOException {
		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(true);
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}

		System.out.println("connected to: " + endpoint);
		TcpConnection con = new TcpConnection(channel, receiveSem);
		// TODO: selector through params
		con.init(SimpleNetSelector.open());
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
	public boolean handle(int selectOp) {
		if (selectOp == SelectionKey.OP_CONNECT) {
			doConnect();
			return false;
		} else if (selectOp == SelectionKey.OP_READ) {
			return doRead();
		} else if (selectOp == SelectionKey.OP_WRITE) {
			return doWrite();
		} else {
			throw new IllegalArgumentException("illegal select-op");
		}
	}

	@Override
	public void closed() {
		// TODO Auto-generated method stub
		 consumer.closed();
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public byte[] peekMessage() {
		return inbox.peek();
	}

	@Override
	public byte[] pollMessage() {
		return inbox.poll();
	}

	@Override
	public byte[] pollMessage(long timeout) {

		// TODO Auto-generated method stub
		return null;
	}

	private void doConnect() {
		// TODO
		System.out.println("op connect");
	}

	private boolean doRead() {
		// TODO: implement
		try {
			rbuf.clear();
			int num = channel.read(rbuf);
			if (num == -1) {
				return true;
			} else {
				rbuf.flip();
				// TODO: make assert
				SimpleValidation.isTrue(num == rbuf.limit());
				SimpleValidation.isTrue(num == rbuf.remaining());
				byte[] b = new byte[num];
				rbuf.get(b);
				SimpleValidation.isTrue(0 == rbuf.remaining());
				// System.out.println("read: " + b.length);
				if (!inbox.offer(b)) {
					return false;
				} else {
					// TODO: log or exception?
					return true;
				}
			}
		} catch (IOException e) {
			// TODO: handle
			// e.printStackTrace();
			return true;
		}
	}

	private AtomicBoolean inSend = new AtomicBoolean(false);

	@Override
	public void send(byte[] data) throws IOException {
		boolean ok = inSend.compareAndSet(false, true);
		SimpleValidation.isTrue(ok);
		try {
			int off = 0;
			int rem = data.length;
			do {
				int n = Math.min(rem, wbuf.capacity());
				_send(data, off, n);
				rem -= n;
				off += n;
			} while (rem > 0);
		} finally {
			inSend.set(false);
		}
	}

	private void _send(byte[] data, int dOff, int wlen) throws IOException {
		// TODO: handle data.length > wbuf.capacity
		wbuf.clear();
		wbuf.put(data, dOff, wlen);
		wbuf.flip();
		// TODO: remove
		SimpleValidation.isTrue(wbuf.remaining() == wlen, wbuf.remaining() + " != " + wlen);
		channel.write(wbuf);
		int rem = wbuf.remaining();
		if (rem > 0) {
			int off = data.length - rem;
			ringBuffer.write(data, off, rem);
			registerForWrite();
			// System.out.println("wrote: " + off + " / " + data.length +
			// ", bufSize=" + ringBuffer.available());
		} else {
			// System.out.println("wrote: " + data.length);
		}
	}

	private boolean doWrite() {
		System.out.println("i am now writable, wheeee :)");
		byte[] transfer = new byte[4096];
		int toRead = Math.min(transfer.length, ringBuffer.available());
		ringBuffer.read(transfer, 0, toRead);
		try {
			send(transfer);
			if (ringBuffer.available() == 0) {
				unregisterForWrite();
			}
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
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
