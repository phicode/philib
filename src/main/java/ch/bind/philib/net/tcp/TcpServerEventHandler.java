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
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.net.events.EventUtil;
import ch.bind.philib.validation.Validation;

final class TcpServerEventHandler extends EventHandlerBase {

	private final ServerSocketChannel channel;

	private final TcpServer server;

	TcpServerEventHandler(NetContext context, ServerSocketChannel channel, TcpServer server) {
		super(context);
		Validation.notNull(channel);
		Validation.notNull(server);
		this.channel = channel;
		this.server = server;
	}

	void start(NetContext context) throws IOException {
		channel.configureBlocking(false);
		context.getEventDispatcher().register(this, EventUtil.ACCEPT);
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void handle(int ops) throws IOException {
		assert (ops == EventUtil.ACCEPT);
		while (true) {
			SocketChannel clientChannel = channel.accept();
			if (clientChannel == null) {
				// no more connections to accept
				return;
			}
			server.createSession(clientChannel);
		}
	}
}
