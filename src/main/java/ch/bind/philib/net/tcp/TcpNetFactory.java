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
import java.util.concurrent.Future;

import ch.bind.philib.net.NetFactory;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.context.NetContext;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class TcpNetFactory implements NetFactory {

	public static final TcpNetFactory INSTANCE = new TcpNetFactory();

	private TcpNetFactory() {
	}

	// TODO: supply session directly!
	@Override
	public Session syncOpenClient(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory)
	        throws IOException {
		if (context.isDebugMode()) {
			return DebugTcpConnection.syncOpen(context, endpoint, sessionFactory);
		}
		return TcpConnection.syncOpen(context, endpoint, sessionFactory);
	}

	@Override
	public Future<Session> asyncOpenClient(NetContext context, SocketAddress endpoint, SessionFactory sessionFactory)
	        throws IOException {
		if (context.isDebugMode()) {
			return DebugTcpConnection.asyncOpen(context, endpoint, sessionFactory);
		}
		return TcpConnection.asyncOpen(context, endpoint, sessionFactory);
	}

	@Override
	public NetServer openServer(NetContext context, SocketAddress bindAddress, SessionFactory consumerFactory)
	        throws IOException {
		return TcpServer.open(context, consumerFactory, bindAddress);
	}
}
