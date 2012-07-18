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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class TcpServer extends EventHandlerBase implements NetServer {

	private static final Logger LOG = LoggerFactory.getLogger(TcpServer.class);

	private final SessionFactory sessionFactory;

	private final ServerSocketChannel channel;

	private TcpServer(NetContext context, SessionFactory sessionFactory, ServerSocketChannel channel) {
		super(context);
		Validation.notNull(sessionFactory);
		Validation.notNull(channel);
		this.sessionFactory = sessionFactory;
		this.channel = channel;
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	@Override
	public void close() throws IOException {
		// the event-dispatcher closes still-open client connections
		context.getEventDispatcher().unregister(this);
		SafeCloseUtil.close(channel, LOG);
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public int handle(int ops) throws IOException {
		assert (ops == EventUtil.ACCEPT);
		while (true) {
			SocketChannel clientChannel = channel.accept();
			if (clientChannel == null) {
				// no more connections to accept
				break;
			}
			createSession(clientChannel);
		}
		return EventUtil.ACCEPT;
	}

	static TcpServer open(NetContext context, SessionFactory sessionFactory, SocketAddress bindAddress)
			throws IOException {
		ServerSocketChannel channel = ServerSocketChannel.open();
		ServerSocket socket = channel.socket();
		int backlog = context.getTcpServerSocketBacklog();
		socket.bind(bindAddress, backlog);
		// TODO: log bridge
		System.out.println("TCP listening on: " + bindAddress);

		TcpServer server = new TcpServer(context, sessionFactory, channel);
		server.setup(context);
		return server;
	}

	private void setup(NetContext context) throws IOException {
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());
		context.getEventDispatcher().register(this, EventUtil.ACCEPT);
	}

	private void createSession(SocketChannel clientChannel) {
		try {
			if (context.isDebugMode()) {
				DebugTcpConnection.create(context, clientChannel, sessionFactory);
			} else {
				TcpConnection.create(context, clientChannel, sessionFactory);
			}
		} catch (IOException e) {
			// TODO: notify an error handler
			System.err.println("faild to create a tcp connection: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
