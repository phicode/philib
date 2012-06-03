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

import ch.bind.philib.io.Ring;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.NetContext;
import ch.bind.philib.net.PureSession;
import ch.bind.philib.net.sel.SelUtil;
import ch.bind.philib.net.sel.SelectableBase;
import ch.bind.philib.validation.SimpleValidation;

public final class TcpConnection extends SelectableBase implements Connection {

	private static final boolean doWriteTimings = false;

	private final SocketChannel channel;

	private final NetContext context;

	private final PureSession session;

	private static final int WRITESTATE_NO_WRITE = 0;

	private static final int WRITESTATE_NONBLOCK_WRITE = 1;

	private static final int WRITESTATE_BLOCKING_WRITE = 2;

	private final Ring<ByteBuffer> writeQueue = new Ring<ByteBuffer>();

	private boolean registeredForWrite = false;

	private final AtomicReference<Integer> writeState = new AtomicReference<Integer>(WRITESTATE_NO_WRITE);

	private Thread dispatcherThread;

	private TcpConnection(NetContext context, SocketChannel channel, PureSession session) throws IOException {
		SimpleValidation.notNull(context);
		SimpleValidation.notNull(channel);
		SimpleValidation.notNull(session);
		this.context = context;
		this.channel = channel;
		this.session = session;
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	static TcpConnection create(NetContext context, SocketChannel channel, PureSession session) throws IOException {
		channel.configureBlocking(false);
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
			NetContext context = NetContext.createSimple();
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
		// store the current dispatcher thread so that we can verify that the
		// event-handler wont perform a blocking write operation
		dispatcherThread = thread;
		try {
			return doRead();
		} finally {
			dispatcherThread = null;
		}
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
		// read as much data as possible
		while (true) {
			try {
				final ByteBuffer rbuf = getBuffer();
				// TODO: move the clear to the buffer cache implementation
				rbuf.clear();
				// int num = BufferOps.readIntoBuffer(channel, rbuf);
				int num = channel.read(rbuf);
				if (num == -1) {
					// connection closed
					releaseBuffer(rbuf);
					return true;
				}
				else if (num == 0) {
					// no more data to read
					releaseBuffer(rbuf);
					return false;
				}
				else {
					rbuf.flip();
					assert (num == rbuf.limit());
					assert (num == rbuf.remaining());
					session.receive(rbuf);
				}
			} catch (IOException e) {
				// TODO: handle
				// e.printStackTrace();
				System.out.println("closed stream detected in tcp-connection doRead: " + e.getMessage());
				return true;
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

	@Override
	public void sendBlocking(ByteBuffer data) throws IOException, InterruptedException {
		SimpleValidation.notNull(data);
		if (Thread.currentThread() == dispatcherThread) {
			throw new IllegalStateException("cant write in blocking mode from the dispatcher thread");
		}
		synchronized (writeQueue) {
			ByteBuffer pending = writeQueue.poll();
			if (pending == null) {
				pending = data;
			}
			else {
				writeQueue.addBack(data);
			}
			while (pending != null && pending.remaining() > 0) {
				sendNonBlocking(pending);
				if (pending.remaining() > 0) {
					writeQueue.addFront(pending);
					registerForWrite();
					writeQueue.wait();
				}
			}
		}
	}

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
		final long tStart = System.nanoTime();
		synchronized (writeQueue) {
			final long tSync = System.nanoTime() - tStart;
			ByteBuffer toWrite = writeQueue.poll();
			while (toWrite != null) {
				try {
					sendNonBlocking(toWrite);
				} catch (IOException e) {
					return true;
				}
				if (toWrite.remaining() > 0) {
					writeQueue.addFront(toWrite);
					final long tAll = System.nanoTime() - tStart;
					System.out.printf("doWrite-stillRegister tSync=%d, tAll=%d%n", tSync, tAll);
					return false;
				}
				toWrite = writeQueue.poll();
			}
			// the write queue is empty, unregister from write events
			unregisterForWrite();
			writeQueue.notifyAll();
			final long tAll = System.nanoTime() - tStart;
			System.out.printf("doWrite-unregister tSync=%d, tAll=%d%n", tSync, tAll);
			return false;
		}
	}

	private void registerForWrite() {
		if (!registeredForWrite) {
			context.getNetSelector().reRegister(this, SelUtil.READ_WRITE, true);
			registeredForWrite = true;
		}
	}

	private void unregisterForWrite() {
		if (registeredForWrite) {
			context.getNetSelector().reRegister(this, SelUtil.READ, false);
			registeredForWrite = false;
		}
	}

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
