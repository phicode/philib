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
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ch.bind.philib.net.NetContext;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.PureSession;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.sel.SelUtil;
import ch.bind.philib.net.sel.SelectableBase;
import ch.bind.philib.validation.SimpleValidation;

public class TcpServer extends SelectableBase implements NetServer {

	// TODO: configurable
	private static final int DEFAULT_BACKLOG = 25;

	private final NetContext context;

	private final SessionFactory sessionFactory;

	private final ServerSocketChannel channel;

	TcpServer(NetContext context, SessionFactory sessionFactory, ServerSocketChannel channel) {
		SimpleValidation.notNull(context);
		SimpleValidation.notNull(sessionFactory);
		SimpleValidation.notNull(channel);
		this.context = context;
		this.sessionFactory = sessionFactory;
		this.channel = channel;
	}

	// TODO: open(SocketAddress) with default netselector
	static TcpServer open(NetContext context, SessionFactory sessionFactory, SocketAddress bindAddress) throws IOException {
		ServerSocketChannel channel = ServerSocketChannel.open();
		ServerSocket socket = channel.socket();
		socket.bind(bindAddress, DEFAULT_BACKLOG);
		channel.configureBlocking(false);
		// TODO: log bridge
		System.out.println("listening on: " + bindAddress);

		TcpServer server = new TcpServer(context, sessionFactory, channel);
		context.getNetSelector().register(server, SelUtil.ACCEPT);
		return server;
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	@Override
	public SelectableChannel getChannel() {
		// TODO: validate open
		return channel;
	}

	@Override
	public void close() throws IOException {
		// TODO: client connections
		context.getNetSelector().unregister(this);
		channel.close();
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean handleAccept() {
		System.out.println("doAccept");
		doAccept();
		return false;
	}

	@Override
	public void closed() {
		// TODO Auto-generated method stub
		// consumer.closed();
		throw new IllegalStateException();
	}

	private void doAccept() {
		try {
			SocketChannel clientChannel = channel.accept();
			PureSession session = sessionFactory.createSession();
			TcpConnection.create(context, clientChannel, session);
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}

	@Override
	public int getActiveSessionCount() {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}
}
