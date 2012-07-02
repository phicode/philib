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
import java.net.Socket;
import java.net.SocketException;
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

	private static final boolean debugMode = true;

	private static final long LOG_HANDLE_TIME_THRESHOLD_NS = 10000000L;

	private static final long LOG_READ_TIME_THRESHOLD_NS = 5000000L;

	private static final long LOG_WRITE_TIME_THRESHOLD_NS = 5000000L;

	private static final int IO_READ_LIMIT_PER_ROUND = 64 * 1024;

	private static final int IO_WRITE_LIMIT_PER_ROUND = 64 * 1024;

	private static final int IO_LIMIT_NONE = -1;

	private final AtomicLong rx = new AtomicLong(0);

	private final AtomicLong tx = new AtomicLong(0);

	private final SocketChannel channel;

	private final TcpConnection connection;

	// this object also provides the sendLock
	private final Ring<NetBuf> w_writeBacklog = new RingImpl<NetBuf>();

	// TODO: remove, or only in debug mode
	private final Object rlock = new Object();

	// TODO: remove volatile once only the event-handler touches this var
	private volatile ByteBuffer r_partialConsume;

	private volatile boolean registeredForWriteEvt = false;

	private long writeCapacity = IO_LIMIT_NONE; // endless

	private long readOps;

	private long sendOps;

	private long numHandles;

	private boolean lastHandleSendable;

	TcpStreamEventHandler(NetContext context, TcpConnection connection, SocketChannel channel) {
		super(context);
		Validation.notNull(connection);
		Validation.notNull(channel);
		this.connection = connection;
		this.channel = channel;
	}

	void setup() throws IOException {
		channel.configureBlocking(false);
		Socket socket = channel.socket();
		if (context.hasCustomTcpNoDelay()) {
			socket.setTcpNoDelay(context.getTcpNoDelay());
		}
		if (context.hasCustomSndBufSize()) {
			socket.setSendBufferSize(context.getSndBufSize());
		}
		if (context.hasCustomRcvBufSize()) {
			socket.setReceiveBufferSize(context.getRcvBufSize());
		}
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

		if (debugMode) {
			numHandles++;
			long r = readOps;
			long s = sendOps;
			long start = System.nanoTime();
			doHandle(ops);
			long t = System.nanoTime() - start;
			if (t > LOG_HANDLE_TIME_THRESHOLD_NS) {
				long rdiff = readOps - r;
				long sdiff = sendOps - s;
				System.out.printf("handle took %.6fms, read-iops=%d, send-iops=%d, rx=%d, tx=%d%n", //
						(t / 1000000f), rdiff, sdiff, getRx(), getTx());
			}
		}
		else {
			doHandle(ops);
		}
	}

	public void doHandle(int ops) throws IOException {
		synchronized (rlock) {
			if (BitOps.checkMask(ops, EventUtil.READ) || r_partialConsume != null) {
				r_read();
			}
		}

		// TODO: synchronize
		// we can try to write more data if the writable flag is set or if we
		// did not request the writable flag to be set => last write didnt block
		boolean finishedWrite = true;
		if (BitOps.checkMask(ops, EventUtil.WRITE) || !registeredForWriteEvt) {
			lastHandleSendable = BitOps.checkMask(ops, EventUtil.WRITE);
			finishedWrite = w_write();
		}
		if (finishedWrite) {
			connection.notifyWritable();
		}
	}

	private boolean w_write() throws IOException {
		synchronized (w_writeBacklog) {
			boolean finished = sl_sendPendingAsync();
			if (finished) {
				unregisterFromWriteEvents();
			}
			else {
				registerForWriteEvents();
			}
			return finished;
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
		synchronized (w_writeBacklog) {
			w_writeBacklog.clear();
			w_writeBacklog.notifyAll();
		}
		if (r_partialConsume != null) {
			releaseBuffer(r_partialConsume);
			r_partialConsume = null;
			unregisterFromDeliverPartialReads();
		}
		connection.notifyClosed();
	}

	int sendAsync(final ByteBuffer data) throws IOException {
		if (data == null || !data.hasRemaining()) {
			return 0;
		}
		synchronized (w_writeBacklog) {
			// write as much as possible until the os-buffers are full or the
			// write-per-round limit is reached
			boolean finished = sl_sendPendingAsync();

			if (!finished) {
				registerForWriteEvents();
				return 0;
			}

			// all data in the backlog has been written
			// this means that the write backlog is empty
			assert (w_writeBacklog.isEmpty());

			int numWritten = sl_channelWrite(data);

			if (data.hasRemaining()) {
				registerForWriteEvents();
			}
			else {
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
		synchronized (w_writeBacklog) {
			w_writeBacklog.addBack(externBuf);
			while (true) {
				boolean finished = sl_sendPendingAsync();

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
						w_writeBacklog.wait();
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

	// rv: true = all writes finished, false=blocked
	// holding the sendLock is required
	private boolean sl_sendPendingAsync() throws IOException {
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
			final int num = sl_channelWrite(bb);
			assert (num <= rem);
			totalWrite += num;
			if (num == rem) {
				boolean externBufReleased = releaseBuffer(pending);
				if (externBufReleased) {
					w_writeBacklog.notifyAll();
				}
			}
			else {
				// write channel is blocked
				w_writeBacklog.addFront(pending);
				break;
			}
		} while (totalWrite < IO_WRITE_LIMIT_PER_ROUND);
		return false;
	}

	// holding the sendLock is required
	private int sl_channelWrite(final ByteBuffer data) throws IOException {
		int num;
		if (debugMode) {
			sendOps++;
			Validation.isFalse(channel.isBlocking());
			long tStart = System.nanoTime();
			num = channel.write(data);
			long t = System.nanoTime() - tStart;
			if (t >= LOG_WRITE_TIME_THRESHOLD_NS) {
				System.out.printf("write took %.6fms%n", (t / 1000000f));
			}
		}
		else {
			num = channel.write(data);
		}
		// long s = sendOps.incrementAndGet();
		if (num > 0) {
			tx.addAndGet(num);
			// long t = tx.addAndGet(num);
			// if ((t / s) < 6 * 1024) {
			// System.out.println("average send/op: " + (t / s));
			// }
		}
		return num;
	}

	// holding the readLock is required
	private int rl_channelRead(final ByteBuffer rbuf) throws IOException {
		int num;
		if (debugMode) {
			readOps++;
			Validation.isFalse(channel.isBlocking());
			long tStart = System.nanoTime();
			num = channel.read(rbuf);
			long t = System.nanoTime() - tStart;
			if (t >= LOG_READ_TIME_THRESHOLD_NS) {
				System.out.printf("read took: %.6fms%n", (t / 1000000f));
			}
		}
		else {
			num = channel.read(rbuf);
		}
		// long r = readOps.incrementAndGet();
		if (num > 0) {
			rx.addAndGet(num);
			// long t = rx.addAndGet(num);
			// if ((t / r) < 6 * 1024) {
			// System.out.println("average read/op: " + (t / r));
			// }
		}
		return num;
	}

	// holding the readLock is required
	private void r_read() throws IOException {
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
				numChanRead = rl_channelRead(bb);
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
			else {
				if (bb.hasRemaining()) {
					totalRead += numChanRead;
				}
				else {
					// if the read buffer is full we cant continue reading until
					// the client has consumed its pending data.
					break;
				}
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
		}
		else {
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

	private void registerForDeliverPartialReads() {
		context.getEventDispatcher().registerForRedeliverPartialReads(this);
	}

	private void unregisterFromDeliverPartialReads() {
		context.getEventDispatcher().unregisterFromRedeliverPartialReads(this);
	}

	long getRx() {
		return rx.get();
	}

	long getTx() {
		return tx.get();
	}

	public String getDebugInformations() {
		String s;
		synchronized (rlock) {
			synchronized (w_writeBacklog) {
				ByteBuffer r = r_partialConsume;
				if (r != null) {
					s = "read: " + r.position();
				}
				else {
					s = "read-ready=0";
				}
			}
			if (w_writeBacklog.isEmpty()) {
				s += ", write-available=0";
			}
			else {
				s += ", write-available=" + w_writeBacklog.size();
			}
			try {
				s += ", readOps=" + readOps + ", sendOps=" + sendOps + ", reg4send=" + registeredForWriteEvt + ", lasthandle-send="
						+ lastHandleSendable + ", numHandles=" + numHandles + ", rx=" + rx.get() + ", tx=" + tx.get() + ", no-delay="
						+ channel.socket().getTcpNoDelay() + ", rcvBuf=" + channel.socket().getReceiveBufferSize() + ", sndBuf="
						+ channel.socket().getSendBufferSize();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return s;
	}
}
