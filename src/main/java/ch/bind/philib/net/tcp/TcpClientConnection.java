package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

import ch.bind.philib.net.InterestedEvents;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.events.Event;

public class TcpClientConnection extends TcpConnection {

	// not null while connecting
	private volatile AsyncConnectFuture<TcpConnection> future;

	public TcpClientConnection(NetContext context, SocketChannel channel, SocketAddress remoteAddress) {
		super(context, channel, remoteAddress);
	}

	// for connecting channels
	public static Future<TcpConnection> connect(NetContext context, SocketChannel channel, SocketAddress remoteAddress) throws IOException {
		TcpClientConnection conn = new TcpClientConnection(context, channel, remoteAddress);
		AsyncConnectFuture<TcpConnection> future = new AsyncConnectFuture<TcpConnection>(conn);
		conn.future = future;
		conn.setupChannel();
		context.getEventDispatcher().register(conn, Event.CONNECT);
		return future;
	}

	@Override
	public int handle(int events) throws IOException {
		if (Event.hasConnect(events)) {
			// assert that only the connect event is present (no read or write)
			assert (future != null && events == Event.CONNECT);
			finishConnect();
			return interestedEvents.getEventMask();
		} else {
			return super.handle(events);
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
	public void setEvents(InterestedEvents interestedEvents) {
		if (future != null) {
			// while connecting we only want to update the interestedEvents
			// field and not tell the dispatcher
			this.interestedEvents = interestedEvents;
		} else {
			super.setEvents(interestedEvents);
		}
	}
}
