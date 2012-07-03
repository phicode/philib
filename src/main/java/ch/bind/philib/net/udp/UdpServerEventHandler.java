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
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.validation.Validation;

final class UdpServerEventHandler extends EventHandlerBase {

	private static final int IO_READ_LIMIT_PER_ROUND = 16 * 1024;

	private static final int IO_WRITE_LIMIT_PER_ROUND = 16 * 1024;

	private final DatagramChannel channel;

	private final UdpServer server;

	UdpServerEventHandler(NetContext context, DatagramChannel channel, UdpServer server) {
		super(context);
		Validation.notNull(channel);
		Validation.notNull(server);
		this.channel = channel;
		this.server = server;
	}

	void start() throws IOException {
		channel.configureBlocking(false);
		DatagramSocket socket = channel.socket();
//		socket.setBroadcast(on);
//		socket.setReceiveBufferSize(size);
//		socket.setSendBufferSize(size);
		context.getEventDispatcher().register(this, EventUtil.READ);
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}
	
	@Override
	public void handle(int ops) throws IOException {
		// TODO Auto-generated method stub
		
		final ByteBuffer rbuf = acquireBuffer();
		try {
			int totalRead = 0;
			while (totalRead < IO_READ_LIMIT_PER_ROUND) {
				rbuf.clear();
				SocketAddress addr = channel.receive(rbuf);
				if (addr == null) {
					// no more data to read
					assert (rbuf.position() == 0 && rbuf.remaining() == 0);
					return;
				} else {
					rbuf.flip();
					// assert (num == rbuf.limit());
					// assert (num == rbuf.remaining());
					totalRead += rbuf.remaining();
					try {
						server.receive(addr, rbuf);
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

//	@Override
//	public void handleWrite() throws IOException {
//		// TODO Auto-generated method stub
//
//	}

	void sendNonBlocking(SocketAddress addr, ByteBuffer data) {

	}

	void sendBlocking(SocketAddress addr, ByteBuffer data) {

	}
}
