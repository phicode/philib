package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ch.bind.philib.net.events.EventHandlerBase;
import ch.bind.philib.validation.Validation;

class TcpServerEventHandler extends EventHandlerBase {

	private final ServerSocketChannel channel;

	private final TcpServer server;

	TcpServerEventHandler(ServerSocketChannel channel, TcpServer server) {
		super();
		Validation.notNull(channel);
		Validation.notNull(server);
		this.channel = channel;
		this.server = server;
	}

	@Override
	public SelectableChannel getChannel() {
		return channel;
	}

	@Override
	public void closed() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void handleAccept() throws IOException {
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
