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

import ch.bind.philib.net.DatagramSession;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.validation.Validation;

public final class UdpServer implements NetServer {

	private final NetContext context;

	private final DatagramSession session;

	private final DatagramChannel channel;

	private UdpServerEventHandler serverEventHandler;

	UdpServer(NetContext context, DatagramSession session, DatagramChannel channel) {
		Validation.notNull(context);
		Validation.notNull(session);
		Validation.notNull(channel);
		this.context = context;
		this.session = session;
		this.channel = channel;
	}

	static UdpServer open(NetContext context, DatagramSession session, SocketAddress bindAddress) throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		DatagramSocket socket = channel.socket();
		socket.bind(bindAddress);
		// TODO: log bridge
		System.out.println("UDP listening on: " + bindAddress);

		UdpServer server = new UdpServer(context, session, channel);
		server.serverEventHandler = new UdpServerEventHandler(context, channel, server);
		server.serverEventHandler.start();
		return server;
	}

	@Override
	public void close() throws IOException { // TODO
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getActiveSessionCount() { // TODO
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	void receive(SocketAddress addr, ByteBuffer rbuf) {
		try {
			session.receive(addr, rbuf);
		} catch (IOException e) {
			// TODO Auto-generated method stub
			e.printStackTrace(System.err);
		}
	}
}
