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
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.io.BitOps;
import ch.bind.philib.io.Ring;
import ch.bind.philib.io.RingImpl;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.net.events.NetBuf;
import ch.bind.philib.validation.Validation;

abstract class TcpConnectionBase extends EventHandlerBase implements Connection {

	private static final int IO_READ_LIMIT_PER_ROUND = 64 * 1024;

	private static final int IO_WRITE_LIMIT_PER_ROUND = 64 * 1024;

	private final AtomicLong rx = new AtomicLong(0);

	private final AtomicLong tx = new AtomicLong(0);

	private final SocketChannel channel;

	// this object also provides the sendLock
	private final Ring<NetBuf> w_writeBacklog = new RingImpl<NetBuf>();

	private ByteBuffer r_partialConsume;

	private volatile boolean registeredForWriteEvt = false;

	private Session session;

	TcpConnectionBase(NetContext context, SocketChannel channel) {
		super(context);
		Validation.notNull(channel);
		this.channel = channel;
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	private void notifyClosed() {
		if (session != null) {
			session.closed();
		}
	}

	private void notifyWritable() {
		if (session != null) {
			session.writable();
		}
	}

	private void notifyReceive(ByteBuffer rbuf) throws IOException {
		if (session != null) {
			session.receive(rbuf);
		}
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return channel.socket().getRemoteSocketAddress();
		// return channel.getRemoteAddress(); // JDK7
	}

	void setup() throws IOException {
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());
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

		if (BitOps.checkMask(ops, EventUtil.READ) || r_partialConsume != null) {
			handleRead();
		}

		// TODO: synchronize
		// we can try to write more data if the writable flag is set or if we
		// did not request the writable flag to be set => last write didn't
		// block
		boolean finishedWrite = true;
		if (BitOps.checkMask(ops, EventUtil.WRITE) || !registeredForWriteEvt) {
			finishedWrite = w_write();
		}
		if (finishedWrite) {
			notifyWritable();
		}
	}

	private boolean w_write() throws IOException {
		// if (awlock.compareAndSet(false, true)) {
		// try {
		synchronized (w_writeBacklog) {
			boolean finished = sendPendingAsync();
			if (finished) {
				unregisterFromWriteEvents();
			} else {
				registerForWriteEvents();
			}
			return finished;
		}
		// } finally {
		// awlock.set(false);
		// }
		// } else {
		// System.out.println("could not get atomic write lock");
		// return false;
		// }
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
		synchronized (w_writeBacklog) {
			w_writeBacklog.clear();
			w_writeBacklog.notifyAll();
		}
		if (r_partialConsume != null) {
			releaseBuffer(r_partialConsume);
			r_partialConsume = null;
			unregisterFromDeliverPartialReads();
		}
		notifyClosed();
	}

	@Override
	public int sendAsync(final ByteBuffer data) throws IOException {
		if (data == null || !data.hasRemaining()) {
			return 0;
		}
		synchronized (w_writeBacklog) {
			// final int cap = writeCapacity;

			// write as much as possible until the os-buffers are full or the
			// write-per-round limit is reached
			boolean finished = sendPendingAsync();

			if (!finished) {
				registerForWriteEvents();
				return 0;
			}

			// all data in the backlog has been written
			// this means that the write backlog is empty
			assert (w_writeBacklog.isEmpty());

			int numWritten = channelWrite(data);

			if (data.hasRemaining()) {
				registerForWriteEvents();
			} else {
				// backlog and input buffer written
				unregisterFromWriteEvents();
			}
			return numWritten;
		}
	}

	@Override
	public void sendSync(final ByteBuffer data) throws IOException, InterruptedException {
		if (data == null || !data.hasRemaining()) {
			return;
		}
		if (getContext().getEventDispatcher().isEventDispatcherThread(Thread.currentThread())) {
			throw new IllegalStateException("cant write in blocking mode from the dispatcher thread");
		}

		// first the remaining data in the backlog has to be written (if
		// any), then our buffer
		// if in the meantime more data arrives we do not want to block
		// longer
		final NetBuf externBuf = NetBuf.createExtern(data);
		synchronized (w_writeBacklog) {
			w_writeBacklog.addBack(externBuf);
			while (true) {
				boolean finished = sendPendingAsync();

				if (finished) {
					// all data from the backlog has been written
					assert (!externBuf.isPending() && !data.hasRemaining());
					unregisterFromWriteEvents();
					return;
				}
				registerForWriteEvents();

				// not all data in the backlog has been written
				if (externBuf.isPending()) {
					// our data is among those who are waiting to be written
					w_writeBacklog.wait();
				} else {
					// our data has been written
					assert (!data.hasRemaining());
					return;
				}
			}
		}
	}

	// rv: true = all writes finished, false=blocked
	// holding the sendLock is required
	private boolean sendPendingAsync() throws IOException {
		int totalWrite = 0;
		if (w_writeBacklog.isEmpty()) {
			return true;
		}
		do {
			final NetBuf pending = w_writeBacklog.poll();
			if (pending == null) {
				w_writeBacklog.shrink();
				return true;
			}
			final ByteBuffer bb = pending.getBuffer();
			final int rem = bb.remaining();
			assert (rem > 0);
			final int num = channelWrite(bb);
			assert (num <= rem);
			totalWrite += num;
			if (num == rem) {
				boolean externBufReleased = releaseBuffer(pending);
				if (externBufReleased) {
					w_writeBacklog.notifyAll();
				}
			} else {
				// write channel is blocked
				w_writeBacklog.addFront(pending);
				break;
			}
			// } while (true);
		} while (totalWrite < IO_WRITE_LIMIT_PER_ROUND);
		return false;
	}

	// holding the sendLock is required
	int channelWrite(final ByteBuffer data) throws IOException {
		int num = channel.write(data);
		if (num > 0) {
			tx.addAndGet(num);
		}
		return num;
	}

	int channelRead(final ByteBuffer rbuf) throws IOException {
		int num = channel.read(rbuf);
		if (num > 0) {
			rx.addAndGet(num);
		}
		return num;
	}

	private void handleRead() throws IOException {
		ByteBuffer bb = r_partialConsume;
		if (bb == null) {
			bb = acquireBuffer();
			bb.clear();
		}
		int totalRead = 0;
		while (totalRead < IO_READ_LIMIT_PER_ROUND) {
			int numChanRead = 0;
			if (bb.hasRemaining()) {
				// lets try to fill this buffer
				numChanRead = channelRead(bb);
			}
			// deliver data regardless of whether the channel is closed or not
			deliverReadData(bb);
			if (numChanRead == -1) {
				if (bb.position() > 0) {
					System.err.println("connection was closed and the corresponding session did not consume all read data");
				}
				// connection closed
				r_partialConsume = null;
				releaseBuffer(bb);
				close();
				return;
			}
			if (numChanRead == 0) {
				break;
			}
			if (bb.hasRemaining()) {
				totalRead += numChanRead;
			} else {
				// if the read buffer is full we cant continue reading until
				// the client has consumed its pending data.
				break;
			}
		}
		if (bb.position() > 0) {
			// the client did not consume all the data, therefore we do not
			// release this buffer but store it so that we can try to have
			// the client consume it later on
			if (r_partialConsume == null) {
				// not registered for partial consume events
				registerForDeliverPartialReads();
			}
			r_partialConsume = bb;
		} else {
			if (r_partialConsume != null) {
				// registered for partial consume events
				unregisterFromDeliverPartialReads();
			}
			r_partialConsume = null;
			releaseBuffer(bb);
		}
		Validation.isTrue((r_partialConsume == null) || (r_partialConsume.position() > 0));
	}

	private void deliverReadData(ByteBuffer bb) throws IOException {
		if (bb.position() > 0) {
			// switch from write mode to read
			bb.flip();
			notifyReceive(bb);
			// switch back to write mode
			if (bb.hasRemaining()) {
				bb.compact();
			} else {
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

	private void registerForDeliverPartialReads() {
		context.getEventDispatcher().registerForRedeliverPartialReads(this);
	}

	private void unregisterFromDeliverPartialReads() {
		context.getEventDispatcher().unregisterFromRedeliverPartialReads(this);
	}

	@Override
	public long getRx() {
		return rx.get();
	}

	@Override
	public long getTx() {
		return tx.get();
	}

	@Override
	public boolean isWritableNow() {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}
}
