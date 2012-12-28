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
package ch.bind.philib.net.core.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.lang.ServiceState;
import ch.bind.philib.net.core.NetListener;
import ch.bind.philib.net.core.context.NetContext;
import ch.bind.philib.net.core.events.EventHandlerBase;
import ch.bind.philib.net.core.events.SelectOps;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class TcpServer extends EventHandlerBase implements NetListener {

	private static final Logger LOG = LoggerFactory.getLogger(TcpServer.class);

	private final ServiceState serviceState = new ServiceState();

	private final ServerSocketChannel channel;

	private TcpServer(NetContext context, ServerSocketChannel channel) {
		super(context);
		Validation.notNull(channel);
		this.channel = channel;
	}

	@Override
	public NetContext getContext() {
		return context;
	}

	@Override
	public boolean isOpen() {
		return serviceState.isOpen();
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public void close() {
		serviceState.setClosing();
		// the event-dispatcher closes still-open client connections
		context.getEventDispatcher().unregister(this);
		SafeCloseUtil.close(channel, LOG);
		serviceState.setClosed();
	}

	@Override
	public int handleOps(final int ops) throws IOException {
		assert (ops == SelectOps.ACCEPT);
		while (true) {
			SocketChannel clientChannel = channel.accept();
			if (clientChannel == null) {
				// no more connections to accept
				break;
			}
			createSession(clientChannel);
		}
		return SelectOps.ACCEPT;
	}

	@Override
	public boolean handleTimeout() {
		LOG.error("TcpServer.handleTimeout() was unexpectedly called");
		return true;
	}

	static TcpServer listen(NetContext context, SocketAddress bindAddress) throws IOException {
		ServerSocketChannel channel = ServerSocketChannel.open();
		ServerSocket socket = channel.socket();
		int backlog = context.getTcpServerSocketBacklog();
		try {
			socket.bind(bindAddress, backlog);
		} catch (IOException e) {
			SafeCloseUtil.close(channel, LOG);
			throw new IOException("socket binding failed", e);
		}

		try {
			TcpServer server = new TcpServer(context, channel);
			server.setup();
			return server;
		} catch (IOException e) {
			SafeCloseUtil.close(channel);
			throw new IOException("channel setup failed", e);
		}
	}

	private void setup() throws IOException {
		channel.configureBlocking(false);
		context.setSocketOptions(channel.socket());
		serviceState.setOpen();
		context.getEventDispatcher().register(this, SelectOps.ACCEPT);
	}

	private void createSession(final SocketChannel clientChannel) {
		try {
			// TODO:jdk7 SocketAddress remoteAddress =
			// clientChannel.getRemoteAddress();
			// TODO: accept the connection here but create the session on the
			// dispatcher thread
			SocketAddress remoteAddress = clientChannel.socket().getRemoteSocketAddress();
			TcpConnection.createConnected(context, clientChannel, remoteAddress);
			// TODO
			// contextListener.connect(connection);
		} catch (IOException e) {
			// TODO: notify an error handler
			LOG.debug("faild to create a tcp connection: " + ExceptionUtil.buildMessageChain(e));
			SafeCloseUtil.close(clientChannel);
		}
	}
}
