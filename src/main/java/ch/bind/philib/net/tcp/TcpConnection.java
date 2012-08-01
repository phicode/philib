/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class TcpConnection extends TcpConnectionBase {

	private TcpConnection(NetContext context, SocketChannel channel) {
		super(context, channel);
	}

	static TcpConnectionFactory FACTORY = new TcpConnectionFactory() {

		@Override
		public Session create(NetContext context, SocketChannel channel, SessionFactory sessionFactory) throws IOException {
			TcpConnection connection = new TcpConnection(context, channel);
			return connection.setup(sessionFactory);
		}
	};

	public static Session syncOpen(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory)
			throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(true);
		context.setSocketOptions(channel.socket());
		if (!channel.connect(endpoint)) {
			channel.finishConnect();
		}

		return FACTORY.create(context, channel, sessionFactory);
	}

	public static Future<Session> asyncOpen(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory) throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());

		boolean finished = channel.connect(endpoint);
		if (finished) {
			return AsyncConnectHandler.forFinishedConnect(context, channel, sessionFactory, FACTORY);
		} else {
			return AsyncConnectHandler.forPendingConnect(context, channel, sessionFactory, FACTORY);
		}
	}

	@Override
	public String getDebugInformations() {
		return "none";
	}
}
