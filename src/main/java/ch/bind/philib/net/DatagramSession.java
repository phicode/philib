package ch.bind.philib.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import ch.bind.philib.net.context.NetContext;

public interface DatagramSession {

	NetContext getContext();

	void receive(SocketAddress address, ByteBuffer data) throws IOException;

	void send(SocketAddress address, ByteBuffer data) throws IOException;

	void closed();

	// boolean isConnected();
	//
	// boolean isOpen();
	//
	// long getRx();
	//
	// long getTx();
}
