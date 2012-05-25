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
import java.util.concurrent.atomic.AtomicReference;

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.NetContext;
import ch.bind.philib.net.PureSession;
import ch.bind.philib.net.sel.SelUtil;
import ch.bind.philib.net.sel.SelectableBase;
import ch.bind.philib.validation.SimpleValidation;

public class TcpConnection extends SelectableBase implements Connection {

	private static final boolean doWriteTimings = false;

	private final SocketChannel channel;

	private final NetContext context;

	private final PureSession session;

	// private final Object writeLock = new Object();

	private static final int WRITESTATE_NO_WRITE = 0;

	private static final int WRITESTATE_NONBLOCK_WRITE = 1;

	private final AtomicReference<Integer> writeState = new AtomicReference<Integer>(WRITESTATE_NO_WRITE);

	private TcpConnection(NetContext context, SocketChannel channel, PureSession session) throws IOException {
		SimpleValidation.notNull(context);
		SimpleValidation.notNull(channel);
		SimpleValidation.notNull(session);
		this.context = context;
		this.channel = channel;
		this.session = session;
		// TODO: move this somewhere else
		this.channel.configureBlocking(false);
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	static TcpConnection create(NetContext context, SocketChannel channel, PureSession session) throws IOException {
		TcpConnection connection = new TcpConnection(context, channel, session);
		session.init(connection);
		connection.register();
		return connection;
	}

	void register() {
		context.getNetSelector().register(this, SelUtil.READ);
	}

	public static TcpConnection open(SocketAddress endpoint, PureSession session) throws IOException {
		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(true);
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}

		System.out.println("connected to: " + endpoint);
		try {
			NetContext context = NetContext.createDefault();
			return create(context, channel, session);
		} catch (IOException e) {
			closeSafely(channel);
			throw e;
		}
	}

	private static void closeSafely(SocketChannel c) {
		try {
			c.close();
		} catch (IOException e) {
			System.out.println("!!!!! TODO");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		context.getNetSelector().unregister(this);
		channel.close();
		session.closed();
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public boolean handleRead(Thread thread) {
		return doRead();
	}

	@Override
	public boolean handleWrite(Thread thread) {
		return doWrite();
	}

	@Override
	public void closed() {
		// TODO Auto-generated method stub
		session.closed();
	}

	private boolean doRead() {
		// TODO: implement
		// read as much data as possible
		final ByteBuffer rbuf = getBuffer();
		while (true) {
			try {
				// TODO: move the clear to the buffer cache implementation
				rbuf.clear();
				// int num = BufferOps.readIntoBuffer(channel, rbuf);
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
					// byte[] received = new byte[num];
					// rbuf.get(received);
					// SimpleValidation.isTrue(0 == rbuf.remaining());
					session.receive(rbuf);
				}
			} catch (IOException e) {
				// TODO: handle
				// e.printStackTrace();
				return true;
			} finally {
				releaseBuffer(rbuf);
			}
		}
	}

	@Override
	public int send(ByteBuffer data) throws IOException {
		SimpleValidation.notNull(data);

		if (writeState.compareAndSet(WRITESTATE_NO_WRITE, WRITESTATE_NONBLOCK_WRITE)) {
			try {
				return sendNonBlocking(data);
			} finally {
				boolean ok = writeState.compareAndSet(WRITESTATE_NONBLOCK_WRITE, WRITESTATE_NO_WRITE);
				// TODO: SimpleValidation.isTrue => assert
				SimpleValidation.isTrue(ok);
			}
		}
		else {
			// someone else is writing
			return 0;
		}

		// synchronized (writeLock) {
		// // if (writeState == WriteState.WRITE_DIRECTLY) {
		// return _send(data);
		// }
		// else {
		// if (sendQueue.offer(data)) {
		// return data.length;
		// }
		// else {
		// return 0;
		// }
		// }
		// }
	}

	// @Override
	// public void sendBlocking(ByteBuffer data) throws IOException {
	// SimpleValidation.notNull(data);
	// synchronized (writeLock) {
	// if (writeState == WriteState.WRITE_DIRECTLY) {
	// return _send(data);
	// }
	// else {
	// if (sendQueue.offer(data)) {
	// return data.length;
	// }
	// else {
	// return 0;
	// }
	// }
	// }
	// }

	// private int _sendBig(ByteBuffer data) throws IOException {
	// int len = data.length;
	// int off = 0;
	// int rem = len;
	// do {
	// // int n = Math.min(rem, wbuf.capacity());
	// int actual = _send(data, off, n);
	// rem -= actual;
	// off += actual;
	// if (actual != n) {
	// return (len - rem);
	// }
	// } while (rem > 0);
	// return len;
	// }

	private int sendNonBlocking(ByteBuffer data) throws IOException {
		// wbuf.clear();
		// wbuf.put(data, dOff, wlen);
		// wbuf.flip();
		// TODO: remove
		// SimpleValidation.isTrue(data.remaining() > 0, data.remaining() +
		// " <= 0");
		int num;
		if (doWriteTimings) {
			long startNs = System.nanoTime();
			num = channel.write(data);
			long endNs = System.nanoTime();
			long t = endNs - startNs;
			// 3ms
			if (t > 3000000L) {
				System.out.printf("channel.write took %dns, %fms%n", t, (t / 1000000f));
			}
			// if (num < wlen) {
			// registerForWrite();
			// }
		}
		else {
			num = channel.write(data);
		}
		return num;
	}

	private boolean doWrite() {
		// must not be called with the current implementation
		SimpleValidation.isTrue(false);
		return true;
		// synchronized (writeLock) {
		// System.out.println("i am now writable, wheeee :)");
		// byte[] nextBuf = sendQueue.poll();
		// while (nextBuf != null) {
		// int actual;
		// try {
		// throw new IOException("whut");
		// // actual = _sendBig(nextBuf);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// return true;
		// }
		// int numRem = nextBuf.length - actual;
		// // TODO: assert
		// SimpleValidation.notNegative(numRem);
		// if (numRem == 0) {
		// nextBuf = sendQueue.poll();
		// }
		// else {
		// byte[] rem = new byte[numRem];
		// System.arraycopy(nextBuf, actual, rem, 0, numRem);
		// boolean ok = sendQueue.offerFront(rem);
		// SimpleValidation.isTrue(ok);
		// return false;
		// }
		// }
		// if (nextBuf == null) {
		// unregisterForWrite();
		// }
		// return false;
		// }
	}

	// private void registerForWrite() {
	// if (writeState == WriteState.WRITE_DIRECTLY) {
	// netSelector.reRegister(this, SelUtil.READ_WRITE);
	// writeState = WriteState.WRITE_BY_SELECTOR;
	// }
	// else {
	// System.out.println("already registered for write");
	// }
	// }
	//
	// private void unregisterForWrite() {
	// if (writeState == WriteState.WRITE_BY_SELECTOR) {
	// netSelector.reRegister(this, SelUtil.READ);
	// writeState = WriteState.WRITE_DIRECTLY;
	// }
	// else {
	// System.out.println("already unregistered from write");
	// }
	// }

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	private ByteBuffer getBuffer() {
		return context.getBufferCache().acquire();
	}

	private void releaseBuffer(ByteBuffer buf) {
		context.getBufferCache().release(buf);
	}
}
