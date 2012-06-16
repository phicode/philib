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
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.NetContext;
import ch.bind.philib.net.PureSession;
import ch.bind.philib.validation.Validation;

public final class TcpConnection implements Connection {

	private final SocketChannel channel;

	private final NetContext context;

	private final PureSession session;

	private AtomicBoolean reading = new AtomicBoolean(false);

	private TcpStreamEventHandler eventHandler;

	private TcpConnection(NetContext context, SocketChannel channel, PureSession session) throws IOException {
		Validation.notNull(context);
		Validation.notNull(channel);
		Validation.notNull(session);
		this.context = context;
		this.channel = channel;
		this.session = session;
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	static TcpConnection create(NetContext context, SocketChannel channel, PureSession session) throws IOException {
		channel.configureBlocking(false);
		TcpConnection connection = new TcpConnection(context, channel, session);
		session.init(connection);
		connection.eventHandler = TcpStreamEventHandler.create(context, connection, channel);
		return connection;
	}

	public static TcpConnection open(SocketAddress endpoint, PureSession session) throws IOException {
		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(true);
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}

		System.out.println("connected to: " + endpoint);
		try {
			NetContext context = NetContext.createSimple();
			return create(context, channel, session);
		} catch (IOException e) {
			closeSafely(channel);
			throw e;
		}
	}

	private static void closeSafely(SocketChannel c) {
		try {
			c.close();
		} catch (IOException e) {
			System.out.println("!!!!! TODO");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void send(ByteBuffer data) throws IOException {
		eventHandler.sendNonBlocking(data);
	}

	@Override
	public void sendBlocking(ByteBuffer data) throws IOException, InterruptedException {
		eventHandler.sendBlocking(data);
	}

	@Override
	public void close() throws IOException {
		eventHandler.close();
	}

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	void notifyClosed() {
		session.closed();
	}
	void receive(ByteBuffer rbuf) throws IOException {
		session.receive(rbuf);
	}

	@Override
	public long getRx() {
		return eventHandler.getRx();
	}

	@Override
	public long getTx() {
		return eventHandler.getTx();
	}
}
