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

import ch.bind.philib.io.Ring;
import ch.bind.philib.net.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.validation.Validation;

class TcpStreamEventHandler extends EventHandlerBase {

	// private static final boolean doWriteTimings = false;
	private static final boolean doWriteTimings = true;

	private static final boolean doReadTimings = true;

	private final SocketChannel channel;

	private final NetContext context;
	
	private final TcpConnection connection;

	private final Ring<ByteBuffer> writeBacklog = new Ring<ByteBuffer>();

	private Thread dispatcherThread;

	TcpStreamEventHandler(SocketChannel channel) {
		super();
		Validation.notNull(channel);
		this.channel = channel;
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public void closed() {
		writeBacklog.clear();
		System.out.println("TODO: propagate EventHandler.closed()");
		// TODO Auto-generated method stub

	}

	@Override
	public void handleRead(final Thread thread) throws IOException {
		// store the current dispatcher thread so that we can verify that the
		// event-handler wont perform a blocking write operation
		dispatcherThread = thread;
		try {
			doRead();
		} finally {
			dispatcherThread = null;
		}
	}

	@Override
	public void handleWrite() {
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	int sendNonBlocking(ByteBuffer data) throws IOException {
		Validation.notNull(data);
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
		return num;
	}

	void sendBlocking(ByteBuffer data) throws IOException, InterruptedException {
		Validation.notNull(data);
		if (Thread.currentThread() == dispatcherThread) {
			throw new IllegalStateException("cant write in blocking mode from the dispatcher thread");
		}
		synchronized (writeBacklog) {
			ByteBuffer pending = writeBacklog.pollNext(data);
			while (pending != null && pending.remaining() > 0) {
				sendNonBlocking(pending);
				if (pending.remaining() > 0) {
					writeBacklog.addFront(pending);
					registerForWrite();
					writeBacklog.wait();
				}
			}
		}
	}

	private void doRead() throws IOException {
		// read as much data as possible
		while (true) {
			final ByteBuffer rbuf = getBuffer();
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
			if (num == -1) {
				// connection closed
				releaseBuffer(rbuf);
				close();
				return;
			}
			else if (num == 0) {
				// no more data to read
				releaseBuffer(rbuf);
				return;
			}
			else {
				rbuf.flip();
				assert (num == rbuf.limit());
				assert (num == rbuf.remaining());
				connection.receive(rbuf);
			}
		}
	}

	private ByteBuffer getBuffer() {
		return context.getBufferCache().acquire();
	}

	private void releaseBuffer(ByteBuffer buf) {
		context.getBufferCache().release(buf);
	}
}
