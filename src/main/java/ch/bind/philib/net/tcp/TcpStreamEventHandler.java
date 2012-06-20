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

import ch.bind.philib.io.Ring;
import ch.bind.philib.io.RingImpl;
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.net.events.NetBuf;
import ch.bind.philib.validation.Validation;

final class TcpStreamEventHandler extends EventHandlerBase {

	private static final boolean doWriteTimings = false;

	private static final boolean doReadTimings = false;

	private static final boolean doHandleReadTimings = true;

	private static final boolean doHandleWriteTimings = true;

	private static final int IO_READ_LIMIT_PER_ROUND = 16 * 1024;

	private static final int IO_WRITE_LIMIT_PER_ROUND = 16 * 1024;

	private final AtomicLong rx = new AtomicLong(0);

	private final AtomicLong tx = new AtomicLong(0);

	private final SocketChannel channel;

	private final TcpConnection connection;

	private final Ring<NetBuf> writeBacklog = new RingImpl<NetBuf>();

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
	public void handleRead() throws IOException {
		if (doHandleReadTimings) {
			long s = System.nanoTime();
			doRead();
			long t = System.nanoTime() - s;
			if (t > 5000000L) { // 5ms
				System.out.printf("handleWrite took %.6fms%n", (t / 1000000f));
			}
		}
		else {
			doRead();
		}
	}

	@Override
	public void handleWrite() throws IOException {
		if (doHandleWriteTimings) {
			long s = System.nanoTime();
			sendNonBlocking(null);
			long t = System.nanoTime() - s;
			if (t > 5000000L) { // 5ms
				System.out.printf("handleWrite took %.6fms%n", (t / 1000000f));
			}
		}
		else {
			sendNonBlocking(null);
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
		synchronized (writeBacklog) {
			writeBacklog.clear();
		}
		connection.notifyClosed();
	}

	void sendNonBlocking(final ByteBuffer data) throws IOException {
		synchronized (writeBacklog) {
			// write as much as possible until the os-buffers are full or the
			// write-per-round limit is reached
			boolean finished = s_sendPendingNonBlock();

			if (!finished) {
				s_copyIntoBacklog(data);
				registerForWriteEvents();
				return;
			}

			// all data in the backlog has been written
			// this means that the write backlog is empty
			assert (writeBacklog.isEmpty());

			if (data != null) {
				_channelWrite(data);

				if (data.hasRemaining()) {
					s_copyIntoBacklog(data);
					registerForWriteEvents();
					return;
				}
			}

			// backlog and input buffer written
			writeBacklog.shrink();
			unregisterFromWriteEvents();
			// notify blocking writes
			writeBacklog.notifyAll();
		}
	}

	void sendBlocking(final ByteBuffer data) throws IOException, InterruptedException {
		if (connection.getContext().getEventDispatcher().isEventDispatcherThread(Thread.currentThread())) {
			throw new IllegalStateException("cant write in blocking mode from the dispatcher thread");
		}

		// first the remaining data in the backlog has to be written (if
		// any), then our buffer
		// if in the meantime more data arrives we do not want to block
		// longer
		final NetBuf externBuf = NetBuf.createExtern(data);
		synchronized (writeBacklog) {
			writeBacklog.addBack(externBuf);
			do {
				boolean finished = s_sendPendingNonBlock();

				if (finished) {
					Validation.isFalse(externBuf.isPending());
					
					// all data from the backlog has been written
					writeBacklog.shrink();
					unregisterFromWriteEvents();

					// notify other blocking writes
					writeBacklog.notifyAll();
					return;
				}
				else {
					// not all data has been written
					if (externBuf.isPending()) {
						// our data is among those who are waiting to be written
						writeBacklog.wait();
					}
					else {
						// our data has been written
						writeBacklog.notifyAll();
						return;
					}
				}
			} while (true);
		}
	}

	private void s_copyIntoBacklog(final ByteBuffer src) {
		if (src == null) {
			return;
		}
		int srcRem = src.remaining();
		while (srcRem > 0) {
			ByteBuffer dst = acquireBuffer();
			NetBuf buf = NetBuf.createIntern(dst);
			int dstCap = dst.capacity();
			if (dstCap >= srcRem) {
				dst.put(src);
				dst.flip();
				assert (dst.remaining() == srcRem);
				writeBacklog.addBack(buf);
				return;
			}
			else {
				final int limit = src.limit();
				int pos = src.position();
				src.limit(pos + dstCap);

				// remaining = limit - position
				// remaining = (position + dstCap) - position;
				// remaining = dstCap
				assert (src.remaining() == dstCap);
				dst.put(src);
				dst.flip();
				assert (dst.remaining() == dstCap);
				writeBacklog.addBack(buf);
				src.limit(limit);
				srcRem -= dstCap;
			}
		}
	}

	// rv: true = all writes finished, false=blocked
	private boolean s_sendPendingNonBlock() throws IOException {
		int totalWrite = 0;
		do {
			final NetBuf pending = writeBacklog.poll();
			if (pending == null) {
				// finished
				unregisterFromWriteEvents();
				return true;
			}
			final ByteBuffer bb = pending.getBuffer();
			final int rem = bb.remaining();
			if (rem == 0) {
				boolean externBufReleased = releaseBuffer(pending);
				if (externBufReleased) {
					writeBacklog.notifyAll();
				}
			}
			else {
				final int num = _channelWrite(bb);
				totalWrite += num;
				if (num == rem) {
					releaseBuffer(pending);
				}
				else {
					// write channel is blocked
					writeBacklog.addFront(pending);
					break;
				}
			}
		} while (totalWrite < IO_WRITE_LIMIT_PER_ROUND);
		registerForWriteEvents();
		return false;
	}

	private int _channelWrite(final ByteBuffer data) throws IOException {
		int num;
		if (doWriteTimings) {
			long tStart = System.nanoTime();
			num = channel.write(data);
			long t = System.nanoTime() - tStart;
			if (t > 2000000L) {
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
		rbuf.clear();
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
		final ByteBuffer rbuf = acquireBuffer();
		try {
			int totalRead = 0;
			while (totalRead < IO_READ_LIMIT_PER_ROUND) {
				int num = _channelRead(rbuf);
				if (num == -1) {
					// connection closed
					close();
					return;
				}
				else if (num == 0) {
					// no more data to read
					return;
				}
				else {
					rbuf.flip();
					assert (num == rbuf.limit());
					assert (num == rbuf.remaining());
					totalRead += num;
					try {
						connection.receive(rbuf);
					} catch (Exception e) {
						System.err.println("TODO: " + ExceptionUtil.buildMessageChain(e));
						e.printStackTrace(System.err);
						close();
					}
				}
			}
		} finally {
			releaseBuffer(rbuf);
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