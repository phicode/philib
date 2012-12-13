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
package ch.bind.philib.net.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ArrayUtil;
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.lang.ServiceState;
import ch.bind.philib.net.DatagramSession;
import ch.bind.philib.net.NetListener;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.SelectOps;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class UdpServer extends EventHandlerBase implements NetListener {

	private static final Logger LOG = LoggerFactory.getLogger(UdpServer.class);

	private static final int IO_READ_LIMIT_PER_ROUND = 64 * 1024;

	private final ServiceState serviceState = new ServiceState();

	private final DatagramChannel channel;

	private final DatagramSession session;

	UdpServer(NetContext context, DatagramChannel channel, DatagramSession session) {
		super(context);
		Validation.notNull(session);
		Validation.notNull(channel);
		this.session = session;
		this.channel = channel;
	}

	void setup() throws IOException {
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());
		serviceState.setOpen();
		context.getEventDispatcher().register(this, SelectOps.READ_WRITE);
	}

	public static UdpServer open(NetContext context, SocketAddress bindAddress, DatagramSession session) throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		DatagramSocket socket = channel.socket();
		try {
			socket.bind(bindAddress);
		} catch (SocketException e) {
			SafeCloseUtil.close(channel, LOG);
			throw new IOException("socket binding failed", e);
		}
		try {
			UdpServer server = new UdpServer(context, channel, session);
			server.setup();
			return server;
		} catch (IOException e) {
			SafeCloseUtil.close(channel);
			throw new IOException("channel setup failed", e);
		}
	}

	@Override
	public boolean isOpen() {
		return serviceState.isOpen();
	}

	@Override
	public void close() {
		serviceState.setClosing();
		context.getEventDispatcher().unregister(this);
		SafeCloseUtil.close(channel, LOG);
		serviceState.setClosed();
		session.closed();
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public int handleOps(int ops) throws IOException {
		int totalRead = 0;
		final ByteBuffer rbuf = takeBuffer();
		while (totalRead < IO_READ_LIMIT_PER_ROUND) {
			if (totalRead > 0) {
				// clear the data from the first read
				ArrayUtil.memsetZero(rbuf);
			}
			// TODO: jumbo-frames could be bigger then the default read-buffer
			// size (8192), detect that or expose a
			// NetContext configuration parameter which allows for jumbo-frames

			SocketAddress addr;
			try {
				addr = channel.receive(rbuf);
			} catch (SecurityException e) {
				// this is a possible runtime-exception, quite nasty
				if (LOG.isTraceEnabled()) {
					LOG.trace("a security-manager blocked an incoming UDP packet");
				}
				continue;
			}
			if (addr == null) {
				// no more data to read
				assert (rbuf.position() == 0 && rbuf.limit() == rbuf.capacity());
				return SelectOps.READ;
			}
			rbuf.flip();
			totalRead += rbuf.remaining();
			notifyReceive(addr, rbuf);
		}
		recycleBuffer(rbuf);
		return SelectOps.READ;
	}

	@Override
	public boolean handleTimeout() {
		LOG.error("UdpServer.handleTimeout() was unexpectedly called");
		return true;
	}

	private void notifyReceive(SocketAddress addr, ByteBuffer data) {
		try {
			session.receive(addr, data);
		} catch (Exception e) {
			LOG.error("notifying an UDP session about a packet from {} failed: {}", addr, ExceptionUtil.buildMessageChain(e));
		}
	}

	void send(final SocketAddress addr, final ByteBuffer data) throws IOException {
		Validation.notNull(addr);
		if (data == null || !data.hasRemaining()) {
			return;
		}
	}
}
