package ch.bind.philib.net;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface PureSession extends Closeable {

	void init(Connection connection);

	void receive(ByteBuffer data);

	int send(ByteBuffer data) throws IOException;

	void closed();

}
