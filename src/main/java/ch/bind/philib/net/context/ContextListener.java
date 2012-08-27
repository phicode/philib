package ch.bind.philib.net.context;

import java.net.SocketAddress;

import ch.bind.philib.net.Session;

public interface ContextListener {

	void sessionCreated(Session session, SocketAddress remoteAddress);

	void sessionClosed(Session session);

	void sessionClosed(Session session, Throwable cause);
}
