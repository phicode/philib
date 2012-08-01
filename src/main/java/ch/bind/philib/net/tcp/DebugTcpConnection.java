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
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.io.BitOps;
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class DebugTcpConnection extends TcpConnectionBase {

	private static final Logger LOG = LoggerFactory.getLogger(DebugTcpConnection.class);

	private static final long LOG_HANDLE_TIME_THRESHOLD_NS = 10000000L;

	private static final long LOG_READ_TIME_THRESHOLD_NS = 5000000L;

	private static final long LOG_WRITE_TIME_THRESHOLD_NS = 5000000L;

	private AtomicLong readOps = new AtomicLong();

	private AtomicLong sendOps = new AtomicLong();

	private AtomicLong numHandles = new AtomicLong();

	private volatile boolean lastHandleSendable;

	private DebugTcpConnection(NetContext context, SocketChannel channel) {
		super(context, channel);
	}

	static TcpConnectionFactory FACTORY = new TcpConnectionFactory() {

		@Override
		public Session create(NetContext context, SocketChannel channel, SessionFactory sessionFactory) throws IOException {
			DebugTcpConnection connection = new DebugTcpConnection(context, channel);
			return connection.setup(sessionFactory);
		}
	};

	public static Session syncOpen(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory)
			throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(true);
		context.setSocketOptions(channel.socket());
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}

		return FACTORY.create(context, channel, sessionFactory);
	}

	public static Future<Session> asyncOpen(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory) throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());

		boolean finished = channel.connect(endpoint);
		if (finished) {
			return AsyncConnectHandler.forFinishedConnect(context, channel, sessionFactory, FACTORY);
		} else {
			return AsyncConnectHandler.forPendingConnect(context, channel, sessionFactory, FACTORY);
		}
	}

	@Override
	public int handle(int ops) throws IOException {
		lastHandleSendable = BitOps.checkMask(ops, EventUtil.WRITE);
		numHandles.incrementAndGet();
		long r = readOps.get();
		long s = sendOps.get();
		long start = System.nanoTime();
		int rv = super.handle(ops);
		long t = System.nanoTime() - start;
		if (t > LOG_HANDLE_TIME_THRESHOLD_NS) {
			long rdiff = readOps.get() - r;
			long sdiff = sendOps.get() - s;
			LOG.debug(String.format("handle took %.6fms, read-iops=%d, send-iops=%d, rx=%d, tx=%d%n", //
					(t / 1000000f), rdiff, sdiff, getRx(), getTx()));
		}
		return rv;
	}

	@Override
	int channelWrite(ByteBuffer data) throws IOException {
		sendOps.incrementAndGet();
		Validation.isFalse(channel.isBlocking());
		long tStart = System.nanoTime();
		int num = super.channelWrite(data);
		long t = System.nanoTime() - tStart;
		if (t >= LOG_WRITE_TIME_THRESHOLD_NS) {
			LOG.debug(String.format("write took %.6fms%n", (t / 1000000f)));
		}
		return num;
	}

	@Override
	int channelRead(ByteBuffer rbuf) throws IOException {
		readOps.incrementAndGet();
		Validation.isFalse(channel.isBlocking());
		long tStart = System.nanoTime();
		int num = super.channelRead(rbuf);
		long t = System.nanoTime() - tStart;
		if (t >= LOG_READ_TIME_THRESHOLD_NS) {
			LOG.debug(String.format("read took: %.6fms%n", (t / 1000000f)));
		}
		return num;
	}

	@Override
	public String getDebugInformations() {
		try {
			Socket sock = channel.socket();
			String m = "readOps=%d, sendOps=%d, reg4send=%s, lastHandleSendable=%s, numHandles=%d, rx=%d, tx=%d, tcp-no-delay=%s, rcvBuf=%d, sndBuf=%d";
			return String.format(m, readOps, sendOps, isRegisteredForWriteEvents(), lastHandleSendable, numHandles,
					getRx(), getTx(), sock.getTcpNoDelay(), sock.getReceiveBufferSize(), sock.getSendBufferSize());
		} catch (SocketException e) {
			return "error: " + ExceptionUtil.buildMessageChain(e);
		}
	}
}
