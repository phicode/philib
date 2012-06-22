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
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.io.BitOps;
import ch.bind.philib.io.Ring;
import ch.bind.philib.io.RingImpl;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.net.events.NetBuf;
import ch.bind.philib.validation.Validation;

final class TcpStreamEventHandler extends EventHandlerBase {

	private static final boolean doWriteTimings = false;

	private static final boolean doReadTimings = false;

	private static final boolean doHandleTimings = true;

	private static final int IO_READ_LIMIT_PER_ROUND = 64 * 1024;

	private static final int IO_WRITE_LIMIT_PER_ROUND = 64 * 1024;

	private final AtomicLong rx = new AtomicLong(0);

	private final AtomicLong tx = new AtomicLong(0);

	private final SocketChannel channel;

	private final TcpConnection connection;

	private final Ring<NetBuf> w = new RingImpl<NetBuf>();

	private ByteBuffer _rbuf;

	private boolean registeredForWriteEvt = false;

	TcpStreamEventHandler(NetContext context, TcpConnection connection, SocketChannel channel) {
		super(context);
		Validation.notNull(connection);
		Validation.notNull(channel);
		this.connection = connection;
		this.channel = channel;
	}

	void start() throws IOException {
		channel.configureBlocking(false);
		context.getEventDispatcher().register(this, EventUtil.READ);
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public void handle(int ops) throws IOException {
		// only the read and/or write flags may be set
		assert ((ops & EventUtil.READ_WRITE) != 0 && (ops & ~EventUtil.READ_WRITE) == 0);

		if (doHandleTimings) {
			long s = System.nanoTime();
			doHandle(ops);
			long t = System.nanoTime() - s;
			if (t > 5000000L) { // 5ms
				System.out.printf("handle took %.6fms%n", (t / 1000000f));
			}
		}
		else {
			doHandle(ops);
		}
	}

	public void doHandle(int ops) throws IOException {
		if (BitOps.checkMask(ops, EventUtil.READ)) {
			doRead();
		}

		// TODO: synchronize
		// we can try to write more data if the writable flag is set or if we
		// did not request the writable flag to be set => last write didnt block
		if (BitOps.checkMask(ops, EventUtil.WRITE) || !registeredForWriteEvt) {
			doWrite();
		}
	}

	private void doWrite() throws IOException {
		synchronized (w) {
			boolean finished = locked_sendPendingAsync();
			if (finished) {
				unregisterFromWriteEvents();
			}
			else {
				registerForWriteEvents();
			}
		}
	}

	@Override
	public void close() {
		context.getEventDispatcher().unregister(this);
		try {
			if (channel.isOpen()) {
				channel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		synchronized (w) {
			_rbuf = null;
			w.clear();
			w.notifyAll();
		}
		connection.notifyClosed();
	}

	int sendAsync(final ByteBuffer data) throws IOException {
		if (data == null || !data.hasRemaining()) {
			return 0;
		}
		synchronized (w) {
			// write as much as possible until the os-buffers are full or the
			// write-per-round limit is reached
			boolean finished = locked_sendPendingAsync();

			if (!finished) {
				registerForWriteEvents();
				return 0;
			}

			// all data in the backlog has been written
			// this means that the write backlog is empty
			assert (w.isEmpty());

			int numWritten = _channelWrite(data);

			if (!data.hasRemaining()) {
				// backlog and input buffer written
				unregisterFromWriteEvents();
			}
			return numWritten;
		}
	}

	void sendSync(final ByteBuffer data) throws IOException, InterruptedException {
		if (data == null || !data.hasRemaining()) {
			return;
		}
		if (connection.getContext().getEventDispatcher().isEventDispatcherThread(Thread.currentThread())) {
			throw new IllegalStateException("cant write in blocking mode from the dispatcher thread");
		}

		// first the remaining data in the backlog has to be written (if
		// any), then our buffer
		// if in the meantime more data arrives we do not want to block
		// longer
		final NetBuf externBuf = NetBuf.createExtern(data);
		synchronized (w) {
			w.addBack(externBuf);
			while (true) {
				boolean finished = locked_sendPendingAsync();

				if (finished) {
					// all data from the backlog has been written
					assert (!externBuf.isPending() && !data.hasRemaining());
					unregisterFromWriteEvents();
					return;
				}
				else {
					registerForWriteEvents();

					// not all data in the backlog has been written
					if (externBuf.isPending()) {
						// our data is among those who are waiting to be written
						w.wait();
					}
					else {
						// our data has been written
						assert (!data.hasRemaining());
						return;
					}
				}
			}
		}
	}

	// private void s_copyIntoBacklog(final ByteBuffer src) {
	// if (src == null) {
	// return;
	// }
	// int srcRem = src.remaining();
	// while (srcRem > 0) {
	// ByteBuffer dst = acquireBuffer();
	// NetBuf buf = NetBuf.createIntern(dst);
	// int dstCap = dst.capacity();
	// if (dstCap >= srcRem) {
	// dst.put(src);
	// dst.flip();
	// assert (dst.remaining() == srcRem);
	// writeBacklog.addBack(buf);
	// return;
	// }
	// else {
	// final int limit = src.limit();
	// int pos = src.position();
	// src.limit(pos + dstCap);
	//
	// // remaining = limit - position
	// // remaining = (position + dstCap) - position;
	// // remaining = dstCap
	// assert (src.remaining() == dstCap);
	// dst.put(src);
	// dst.flip();
	// assert (dst.remaining() == dstCap);
	// writeBacklog.addBack(buf);
	// src.limit(limit);
	// srcRem -= dstCap;
	// }
	// }
	// }

	// rv: true = all writes finished, false=blocked
	private boolean locked_sendPendingAsync() throws IOException {
		int totalWrite = 0;
		do {
			final NetBuf pending = w.poll();
			if (pending == null) {
				w.shrink();
				return true;
			}
			final ByteBuffer bb = pending.getBuffer();
			final int rem = bb.remaining();
			if (rem == 0) {
				boolean externBufReleased = releaseBuffer(pending);
				if (externBufReleased) {
					w.notifyAll();
				}
			}
			else {
				final int num = _channelWrite(bb);
				totalWrite += num;
				if (num == rem) {
					boolean externBufReleased = releaseBuffer(pending);
					if (externBufReleased) {
						w.notifyAll();
					}
				}
				else {
					// write channel is blocked
					w.addFront(pending);
					break;
				}
			}
		} while (totalWrite < IO_WRITE_LIMIT_PER_ROUND);
		return false;
	}

	private int _channelWrite(final ByteBuffer data) throws IOException {
		int num;
		if (doWriteTimings) {
			long tStart = System.nanoTime();
			num = channel.write(data);
			long t = System.nanoTime() - tStart;
			if (t > 2000000) {
				System.out.printf("write took %.6fms%n", (t / 1000000f));
			}
		}
		else {
			num = channel.write(data);
		}
		tx.addAndGet(num);
		return num;
	}

	private int _channelRead(final ByteBuffer rbuf) throws IOException {
		int num;
		if (doReadTimings) {
			long tStart = System.nanoTime();
			num = channel.read(rbuf);
			long t = System.nanoTime() - tStart;
			if (t > 2000000) {
				System.out.printf("read took: %.6fms%n", (t / 1000000f));
			}
		}
		else {
			num = channel.read(rbuf);
		}
		if (num > 0) {
			rx.addAndGet(num);
		}
		return num;
	}

	private void doRead() throws IOException {
		ByteBuffer bb = _rbuf;
		if (bb == null) {
			bb = acquireBuffer();
			bb.clear();
		}
		int totalRead = 0;
		while (totalRead < IO_READ_LIMIT_PER_ROUND) {
			int numChanRead = 0;
			if (bb.hasRemaining()) {
				// lets try to fill this buffer
				numChanRead = _channelRead(bb);
				if (numChanRead == -1) {
					deliverReadData(bb);
					if (bb.position() > 0) {
						System.err.println("connection was closed and a client-session did not consume all read data");
					}
					// connection closed
					_rbuf = null;
					releaseBuffer(bb);
					close();
					return;
				}
			}
			deliverReadData(bb);
			if (bb.position() > 0) {
				// the client did not consume all the data, therefore we do not
				// release this buffer but store it so that we can try to have
				// the client consume it later on
				_rbuf = bb;
				return;
			}
			else {
				// all data was consumed by the client
				totalRead += numChanRead;
				_rbuf = null;
			}
		}
		if (_rbuf == null) {
			releaseBuffer(bb);
		}
	}

	private void deliverReadData(ByteBuffer bb) throws IOException {
		if (bb.position() > 0) {
			// switch from write mode to read
			bb.flip();
			connection.receive(bb);
			// switch back to write mode
			if (bb.hasRemaining()) {
				bb.compact();
			}
			else {
				bb.clear();
			}
		}
	}

	private void registerForWriteEvents() {
		if (!registeredForWriteEvt) {
			context.getEventDispatcher().reRegister(this, EventUtil.READ_WRITE, true);
			registeredForWriteEvt = true;
		}
	}

	private void unregisterFromWriteEvents() {
		if (registeredForWriteEvt) {
			context.getEventDispatcher().reRegister(this, EventUtil.READ, false);
			registeredForWriteEvt = false;
		}
	}

	long getRx() {
		return rx.get();
	}

	long getTx() {
		return tx.get();
	}
}
