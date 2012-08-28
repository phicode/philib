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
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.io.BitOps;
import ch.bind.philib.lang.ExceptionUtil;
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

	DebugTcpConnection(NetContext context, SocketChannel channel, SocketAddress remoteAddress) {
		super(context, channel, remoteAddress);
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
		if (data.remaining() < 10) {
			LOG.debug("small channel write pending: " + data.remaining());
		}
		sendOps.incrementAndGet();
		Validation.isFalse(channel.isBlocking());
		long tStart = System.nanoTime();
		int num = super.channelWrite(data);
		if (num > 0 && num < 50) {
			LOG.debug("small channel write: " + num);
		}
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
			int ops = context.getEventDispatcher().getRegisteredOps(this);
			String sOps = EventUtil.opsToString(ops);
			String m = "ops=%s, readOps=%s, sendOps=%s, reg4send=%s, lastHandleSendable=%s, numHandles=%s, rx=%d, tx=%d, tcp-no-delay=%s, rcvBuf=%d, sndBuf=%d";
			return String.format(m, sOps, readOps, sendOps, isRegisteredForWriteEvents(), lastHandleSendable, numHandles, getRx(), getTx(), sock.getTcpNoDelay(),
			        sock.getReceiveBufferSize(), sock.getSendBufferSize());
		} catch (SocketException e) {
			return "error: " + ExceptionUtil.buildMessageChain(e);
		}
	}
}
