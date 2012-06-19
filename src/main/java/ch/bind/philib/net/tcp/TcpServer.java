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
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.PureSession;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.validation.Validation;

public final class TcpServer implements NetServer {

	// TODO: configurable
	private static final int DEFAULT_BACKLOG = 25;

	private final NetContext context;

	private final SessionFactory sessionFactory;

	private final ServerSocketChannel channel;

	private TcpServerEventHandler serverEventHandler;

	TcpServer(NetContext context, SessionFactory sessionFactory, ServerSocketChannel channel) {
		Validation.notNull(context);
		Validation.notNull(sessionFactory);
		Validation.notNull(channel);
		this.context = context;
		this.sessionFactory = sessionFactory;
		this.channel = channel;

	}

	static TcpServer open(NetContext context, SessionFactory sessionFactory, SocketAddress bindAddress) throws IOException {
		ServerSocketChannel channel = ServerSocketChannel.open();
		ServerSocket socket = channel.socket();
		socket.bind(bindAddress, DEFAULT_BACKLOG);
		// TODO: log bridge
		System.out.println("TCP listening on: " + bindAddress);

		TcpServer server = new TcpServer(context, sessionFactory, channel);
		server.serverEventHandler = new TcpServerEventHandler(channel, server);
		server.serverEventHandler.start(context);
		return server;
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	@Override
	public void close() throws IOException {
		// TODO: client connections
		context.getEventDispatcher().unregister(serverEventHandler);
		channel.close();
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getActiveSessionCount() {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	void createSession(SocketChannel clientChannel) {
		PureSession session = null;
		try {
			session = sessionFactory.createSession();
		} catch (Exception e) {
			// TODO: logging
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			return;
		}
		try {
			TcpConnection.create(context, clientChannel, session);
		} catch (IOException e) {
			// TODO: notify an error handler
			System.err.println("faild to create a tcp connection: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
