package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

import ch.bind.philib.net.Events;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.SelectOps;

public class TcpClientConnection extends TcpConnection {

	// not null while connecting
	private volatile AsyncConnectFuture<TcpConnection> future;

	public TcpClientConnection(NetContext context, SocketChannel channel, SocketAddress remoteAddress) {
		super(context, channel, remoteAddress);
	}

	// for connecting channels
	static Future<TcpConnection> createConnecting(NetContext context, SocketChannel channel, SocketAddress remoteAddress) throws IOException {
		TcpClientConnection conn = new TcpClientConnection(context, channel, remoteAddress);
		AsyncConnectFuture<TcpConnection> future = new AsyncConnectFuture<TcpConnection>(conn);
		conn.future = future;
		conn.setupChannel();
		context.getEventDispatcher().register(conn, SelectOps.CONNECT);
		return future;
	}

	@Override
	public int handleOps(int ops) throws IOException {
		if (SelectOps.hasConnect(ops)) {
			// assert that only the connect event is present (no read or write)
			assert (future != null && ops == SelectOps.CONNECT);
			finishConnect();
			return events.getEventMask();
		}
		else {
			return super.handleOps(ops);
		}
	}

	private void finishConnect() throws IOException {
		final AsyncConnectFuture<TcpConnection> f = this.future;
		this.future = null;
		try {
			channel.finishConnect();
		} catch (IOException e) {
			context.getSessionManager().connectFailed(remoteAddress, e);
			f.setFailed(e);
			throw e;
		}
		try {
			setupSession();
		} catch (IOException e) {
			f.setFailed(e);
			throw e;
		}
		f.setFinishedOk();
	}

	@Override
	public void setEvents(Events events) {
		if (future != null) {
			// while connecting we only want to update the events
			// field and not tell the dispatcher
			this.events = events;
		}
		else {
			super.setEvents(events);
		}
	}
}
