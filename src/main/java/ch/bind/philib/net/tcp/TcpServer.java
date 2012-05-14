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
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.sel.NetSelector;
import ch.bind.philib.net.sel.SelOps;
import ch.bind.philib.validation.SimpleValidation;

public class TcpServer implements NetServer {

	// TODO: configurable
	private static final int DEFAULT_BACKLOG = 100;

	private NetSelector selector;

	private ServerSocketChannel channel;

	private final SessionFactory sessionFactory;

	TcpServer(SessionFactory sessionFactory) {
		SimpleValidation.notNull(sessionFactory);
		this.sessionFactory = sessionFactory;
	}

	// TODO: open(SocketAddress) with default netselector
	void open(NetSelector selector, SocketAddress bindAddress) throws IOException {
		this.selector = selector;

		ServerSocketChannel channel = ServerSocketChannel.open();
		ServerSocket socket = channel.socket();
		socket.bind(bindAddress, DEFAULT_BACKLOG);
		channel.configureBlocking(false);
		// TODO: log bridge
		System.out.println("listening on: " + bindAddress);
		this.channel = channel;
		selector.register(this, SelOps.ACCEPT);
	}

	@Override
	public SelectableChannel getChannel() {
		// TODO: validate open
		return channel;
	}

	@Override
	public void close() throws IOException {
		// TODO: client connections
		selector.unregister(this);
		channel.close();
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean handle(int selectOp) {
		if (selectOp == SelectionKey.OP_ACCEPT) {
			System.out.println("doAccept");
			doAccept();
			return false;
		}
		else {
			throw new IllegalArgumentException("illegal select-op");
		}
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
			Session session = sessionFactory.createSession();
			TcpConnection.create(clientChannel, session, selector);
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
