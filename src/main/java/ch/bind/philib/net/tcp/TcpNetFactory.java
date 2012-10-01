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
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandler;
import ch.bind.philib.util.FinishedFuture;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class TcpNetFactory {

	private TcpNetFactory() {
	}

	// TODO: supply session directly!
	public static Session syncOpen(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory) throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(true);
		context.setSocketOptions(channel.socket());
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}
		return create(null, context, channel, endpoint, sessionFactory);
	}

	public static Future<Session> asyncOpen(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory) throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());

		boolean finished = channel.connect(endpoint);
		if (finished) {
			Session session = create(null, context, channel, endpoint, sessionFactory);
			return new FinishedFuture<Session>(session);
		}
		return AsyncConnectHandler.create(context, channel, sessionFactory);
	}

	public static NetServer openServer(NetContext context, SocketAddress bindAddress, SessionFactory sessionFactory) throws IOException {
		return TcpServer.open(context, sessionFactory, bindAddress);
	}

	public static Session create(EventHandler oldHandler, NetContext context, SocketChannel channel, SessionFactory sessionFactory) throws IOException {
		SocketAddress remoteAddress = channel.getRemoteAddress();
		return create(oldHandler, context, channel, remoteAddress, sessionFactory);
	}

	public static Session create(EventHandler oldHandler, NetContext context, SocketChannel channel, SocketAddress remoteAddress, SessionFactory sessionFactory)
	        throws IOException {
		TcpConnectionBase connection = null;
		if (context.isDebugMode()) {
			connection = new DebugTcpConnection(context, channel, remoteAddress);
		} else {
			connection = new TcpConnection(context, channel, remoteAddress);
		}
		return connection.setup(oldHandler, sessionFactory);
	}
}
