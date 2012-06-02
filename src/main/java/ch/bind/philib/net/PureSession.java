package ch.bind.philib.net;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface PureSession extends Closeable {

	void init(Connection connection);

	NetContext getContext();

	void receive(ByteBuffer data);

	int send(ByteBuffer data) throws IOException;

	// int send(byte[] data) throws IOException;

	// void sendAll(ByteBuffer data) throws IOException;

	// void sendAll(byte[] data) throws IOException;;

	void closed();

	boolean isConnected();

	boolean isOpen();

	void releaseBuffer(ByteBuffer buffer);
}
