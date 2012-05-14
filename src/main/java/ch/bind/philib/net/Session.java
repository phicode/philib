package ch.bind.philib.net;

import java.io.Closeable;
import java.io.IOException;

public interface Session extends Closeable {

	void init(Connection connection);

	void receive(byte[] data);

	int send(byte[] data) throws IOException;

	void closed();

}
