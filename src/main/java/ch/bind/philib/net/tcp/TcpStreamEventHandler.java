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
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.validation.Validation;

class TcpStreamEventHandler extends EventHandlerBase {

	// private static final boolean doWriteTimings = false;
	private static final boolean doWriteTimings = false;

	private static final boolean doReadTimings = false;

	private static final int IO_READ_LIMIT_PER_ROUND = 16 * 1024;

	private static final int IO_WRITE_LIMIT_PER_ROUND = 16 * 1024;

	private final AtomicLong rx = new AtomicLong(0);

	private final AtomicLong tx = new AtomicLong(0);

	private final SocketChannel channel;

	private final NetContext context;

	private final TcpConnection connection;

	private final Ring<ByteBuffer> writeBacklog = new Ring<ByteBuffer>();

	private boolean registeredForWrite = false;

	private TcpStreamEventHandler(NetContext context, TcpConnection connection, SocketChannel channel) {
		super();
		Validation.notNull(context);
		Validation.notNull(connection);
		Validation.notNull(channel);
		this.context = context;
		this.connection = connection;
		this.channel = channel;
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public void handleRead() throws IOException {
		doRead();
	}

	@Override
	public void handleWrite() throws IOException {
		try {
			long s = System.nanoTime();
			send(null, false, IO_WRITE_LIMIT_PER_ROUND);
			long t = System.nanoTime() - s;
			if (t > 1000000L) { // 1ms
				System.out.printf("handleWrite took %.6fms%n", (t / 1000000f));
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void close() throws IOException {
		context.getEventDispatcher().unregister(this);
		channel.close();
		writeBacklog.clear();
		connection.notifyClosed();
	}

	void sendNonBlocking(final ByteBuffer data) throws IOException {
		try {
			send(data, false, IO_WRITE_LIMIT_PER_ROUND);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	void sendBlocking(final ByteBuffer data) throws IOException, InterruptedException {
		if (connection.getContext().getEventDispatcher().isEventDispatcherThread(Thread.currentThread())) {
			throw new IllegalStateException("cant write in blocking mode from the dispatcher thread");
		} else {
			// first the remaining data in the backlog has to be written (if
			// any), then our buffer
			// if in the meantime more data arrives we do not want to block
			// longer
			// TODO: iolimit
			send(data, true, Integer.MAX_VALUE);
		}
	}

	private void send(final ByteBuffer data, final boolean blocking, final int ioLimit) throws IOException, InterruptedException {
		synchronized (writeBacklog) {
			int totalWrite = 0;
			ByteBuffer pending = writeBacklog.pollNext(data);

			while (pending != null) {
				int rem = pending.remaining();
				if (rem > 0) {
					int n = send(pending);
					totalWrite += n;
					rem -= n;
					if (rem == 0) {
						if (!blocking && totalWrite >= ioLimit) {
							return;
						}
					} else {
						writeBacklog.addFront(pending);
						registerForWrite();
						if (blocking) {
							// continue looping and waiting
							writeBacklog.wait();
						} else {
							// the data is in the write-backlog
							return;
						}
					}
				}
				pending = writeBacklog.poll();
			}

			Validation.isTrue(writeBacklog.isEmpty());
			unregisterForWrite();
			writeBacklog.notifyAll();
		}
	}

	private int send(final ByteBuffer data) throws IOException {
		int num;
		if (doWriteTimings) {
			long tStart = System.nanoTime();
			num = channel.write(data);
			long t = System.nanoTime() - tStart;
			if (t > 2000000L) {
				System.out.printf("write took %.6fms%n", (t / 1000000f));
			}
		} else {
			num = channel.write(data);
		}
		tx.addAndGet(num);
		return num;
	}

	private void doRead() throws IOException {
		// read as much data as possible
		int totalRead = 0;
		while (totalRead < IO_READ_LIMIT_PER_ROUND) {
			final ByteBuffer rbuf = getBuffer();
			int num;
			if (doReadTimings) {
				long tStart = System.nanoTime();
				num = channel.read(rbuf);
				long t = System.nanoTime() - tStart;
				if (t > 2000000) {
					System.out.printf("read took: %.6fms%n", (t / 1000000f));
				}
			} else {
				num = channel.read(rbuf);
			}
			if (num == -1) {
				// connection closed
				releaseBuffer(rbuf);
				close();
				return;
			} else if (num == 0) {
				// no more data to read
				releaseBuffer(rbuf);
				return;
			} else {
				rbuf.flip();
				assert (num == rbuf.limit());
				assert (num == rbuf.remaining());
				totalRead += num;
				rx.addAndGet(num);
				try {
					connection.receive(rbuf);
				} catch (Exception e) {
					System.err.println("TODO: " + ExceptionUtil.buildMessageChain(e));
					e.printStackTrace(System.err);
					close();
				}
			}
		}
	}

	// private boolean doWrite() {
	// final long tStart = System.nanoTime();
	// synchronized (writeBacklog) {
	// final long tSync = System.nanoTime() - tStart;
	// ByteBuffer toWrite = writeBacklog.poll();
	// while (toWrite != null) {
	// try {
	// sendNonBlocking(toWrite);
	// } catch (IOException e) {
	// return true;
	// }
	// if (toWrite.remaining() > 0) {
	// writeBacklog.addFront(toWrite);
	// final long tAll = System.nanoTime() - tStart;
	// System.out.printf("doWrite-stillRegister tSync=%d, tAll=%d%n", tSync,
	// tAll);
	// return false;
	// }
	// toWrite = writeBacklog.poll();
	// }
	// // TODO: notify client code that we can write more stuff
	//
	// // the write queue is empty, unregister from write events
	// unregisterForWrite();
	// writeBacklog.notifyAll();
	// final long tAll = System.nanoTime() - tStart;
	// System.out.printf("doWrite-unregister tSync=%d, tAll=%d%n", tSync, tAll);
	// return false;
	// }
	// }

	private ByteBuffer getBuffer() {
		return context.getBufferCache().acquire();
	}

	private void releaseBuffer(final ByteBuffer buf) {
		context.getBufferCache().release(buf);
	}

	private void registerForWrite() {
		if (!registeredForWrite) {
			context.getEventDispatcher().reRegister(this, EventUtil.READ_WRITE, true);
			registeredForWrite = true;
		}
	}

	private void unregisterForWrite() {
		if (registeredForWrite) {
			context.getEventDispatcher().reRegister(this, EventUtil.READ, false);
			registeredForWrite = false;
		}
	}

	public static TcpStreamEventHandler create(NetContext context, TcpConnection connection, SocketChannel channel) {
		TcpStreamEventHandler rv = new TcpStreamEventHandler(context, connection, channel);
		context.getEventDispatcher().register(rv, EventUtil.READ);
		return rv;
	}

	long getRx() {
		return rx.get();
	}

	long getTx() {
		return tx.get();
	}
}
