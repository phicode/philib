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

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.validation.Validation;

public final class TcpConnection implements Connection {

	private final SocketChannel channel;

	private final NetContext context;

	private Session session;

	private TcpStreamEventHandler eventHandler;

	private TcpConnection(NetContext context, SocketChannel channel) throws IOException {
		Validation.notNull(context);
		Validation.notNull(channel);
		this.context = context;
		this.channel = channel;
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	private static TcpConnection create(NetContext context, SocketChannel channel) throws IOException {
		TcpConnection connection = new TcpConnection(context, channel);
		connection.eventHandler = new TcpStreamEventHandler(context, connection, channel);
		return connection;
	}

	static Session create(NetContext context, SocketChannel channel, SessionFactory sessionFactory) throws IOException {
		TcpConnection connection = create(context, channel);
		// TODO: handle factory exception by connection.close or something
		connection.session = sessionFactory.createSession(connection);
		connection.eventHandler.setup();
		return connection.session;
	}

	public static Session open(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory) throws IOException {
		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(true);
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}

		System.out.println("connected to: " + endpoint);
		return create(context, channel, sessionFactory);
	}

	@Override
	public int sendAsync(ByteBuffer data) throws IOException {
		return eventHandler.sendAsync(data);
	}

	@Override
	public void sendSync(ByteBuffer data) throws IOException, InterruptedException {
		eventHandler.sendSync(data);
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
		if (session != null) {
			session.closed();
		}
	}

	void notifyWritable() {
		if (session != null) {
			session.writable();
		}
	}

	void notifyReceive(ByteBuffer rbuf) throws IOException {
		if (session != null) {
			session.receive(rbuf);
		}
	}

	@Override
	public long getRx() {
		return eventHandler.getRx();
	}

	@Override
	public long getTx() {
		return eventHandler.getTx();
	}

	@Override
	public String getDebugInformations() {
		return eventHandler.getDebugInformations();
	}

	@Override
	public boolean isWritableNow() {
		return eventHandler.isWritableNow();
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return channel.socket().getRemoteSocketAddress();
		// return channel.getRemoteAddress(); // JDK7
	}
}
