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
import java.nio.channels.SocketChannel;

import ch.bind.philib.io.BufferQueue;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.impl.SimpleNetSelector;
import ch.bind.philib.net.sel.SelectableBase;
import ch.bind.philib.net.sel.NetSelector;
import ch.bind.philib.net.sel.SelUtil;
import ch.bind.philib.validation.SimpleValidation;

public class TcpConnection extends SelectableBase implements Connection {

	private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	private final SocketChannel channel;

	private final ByteBuffer rbuf;

	private final ByteBuffer wbuf;

	private final NetSelector netSelector;

	private final Session session;

	private enum WriteState {
		WRITE_DIRECTLY, WRITE_BY_SELECTOR
	}

	private WriteState writeState = WriteState.WRITE_DIRECTLY;

	private final Object writeLock = new Object();

	// TODO: make the access to the sendqueue synchronized, since it can be
	// accessed by external threads or the net-selector
	private final BufferQueue sendQueue = new BufferQueue(DEFAULT_BUFFER_SIZE);

	private TcpConnection(SocketChannel channel, Session session, NetSelector netSelector) throws IOException {
		SimpleValidation.notNull(channel);
		SimpleValidation.notNull(session);
		SimpleValidation.notNull(netSelector);
		this.channel = channel;
		this.session = session;
		this.netSelector = netSelector;
		this.channel.configureBlocking(false);
		this.rbuf = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		this.wbuf = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
	}

	static TcpConnection create(SocketChannel clientChannel, Session session, NetSelector selector) throws IOException {
		TcpConnection connection = new TcpConnection(clientChannel, session, selector);
		session.init(connection);
		connection.register();
		return connection;
	}

	void register() {
		netSelector.register(this, SelUtil.READ);
	}

	public static TcpConnection open(SocketAddress endpoint, Session session) throws IOException {
		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(true);
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}

		System.out.println("connected to: " + endpoint);
		NetSelector sel = SimpleNetSelector.open();

		return create(channel, session, sel);
	}

	@Override
	public void close() throws IOException {
		netSelector.unregister(this);
		channel.close();
		session.closed();
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public boolean handleRead() {
		return doRead();
	}

	@Override
	public boolean handleWrite() {
		return doWrite();
	}

	@Override
	public void closed() {
		// TODO Auto-generated method stub
		session.closed();
	}

	private boolean doRead() {
		// TODO: implement
		while (true) {
			try {
				rbuf.clear();
				int num = channel.read(rbuf);
				if (num == -1) {
					return true;
				}
				else if (num == 0) {
					return false;
				}
				else {
					rbuf.flip();
					// TODO: make assert
					SimpleValidation.isTrue(num == rbuf.limit());
					SimpleValidation.isTrue(num == rbuf.remaining());
					byte[] received = new byte[num];
					rbuf.get(received);
					SimpleValidation.isTrue(0 == rbuf.remaining());
					session.receive(received);
				}
			} catch (IOException e) {
				// TODO: handle
				// e.printStackTrace();
				return true;
			}
		}
	}

	@Override
	public int send(byte[] data) throws IOException {
		if (data == null || data.length == 0) {
			return 0;
		}
		synchronized (writeLock) {
			if (writeState == WriteState.WRITE_DIRECTLY) {
				return _sendBig(data);
			}
			else {
				if (sendQueue.offer(data)) {
					return data.length;
				}
				else {
					return 0;
				}
			}
		}
	}

	private int _sendBig(byte[] data) throws IOException {
		int len = data.length;
		int off = 0;
		int rem = len;
		do {
			int n = Math.min(rem, wbuf.capacity());
			int actual = _send(data, off, n);
			rem -= actual;
			off += actual;
			if (actual != n) {
				return (len - rem);
			}
		} while (rem > 0);
		return len;
	}

	private int _send(byte[] data, int dOff, int wlen) throws IOException {
		wbuf.clear();
		wbuf.put(data, dOff, wlen);
		wbuf.flip();
		// TODO: remove
		SimpleValidation.isTrue(wbuf.remaining() == wlen, wbuf.remaining() + " != " + wlen);
		long startNs = System.nanoTime();
		int num = channel.write(wbuf);
		long endNs = System.nanoTime();
		long t = endNs - startNs;
		// 3ms
		if (t > 3000000L) {
			System.out.printf("channel.write took %dns, %fms%n", t, (t / 1000000f));
		}
		if (num < wlen) {
			registerForWrite();
		}
		return num;
	}

	private boolean doWrite() {
		synchronized (writeLock) {
			System.out.println("i am now writable, wheeee :)");
			byte[] nextBuf = sendQueue.poll();
			while (nextBuf != null) {
				int actual;
				try {
					actual = _sendBig(nextBuf);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return true;
				}
				int numRem = nextBuf.length - actual;
				// TODO: assert
				SimpleValidation.notNegative(numRem);
				if (numRem == 0) {
					nextBuf = sendQueue.poll();
				}
				else {
					byte[] rem = new byte[numRem];
					System.arraycopy(nextBuf, actual, rem, 0, numRem);
					boolean ok = sendQueue.offerFront(rem);
					SimpleValidation.isTrue(ok);
					return false;
				}
			}
			if (nextBuf == null) {
				unregisterForWrite();
			}
			return false;
		}
	}

	private void registerForWrite() {
		if (writeState == WriteState.WRITE_DIRECTLY) {
			netSelector.reRegister(this, SelUtil.READ_WRITE);
			writeState = WriteState.WRITE_BY_SELECTOR;
		}
		else {
			System.out.println("already registered for write");
		}
	}

	private void unregisterForWrite() {
		if (writeState == WriteState.WRITE_BY_SELECTOR) {
			netSelector.reRegister(this, SelUtil.READ);
			writeState = WriteState.WRITE_DIRECTLY;
		}
		else {
			System.out.println("already unregistered from write");
		}
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	public boolean isOpen() {
		return channel.isOpen();
	}
}
