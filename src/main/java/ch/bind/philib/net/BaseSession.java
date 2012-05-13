package ch.bind.philib.net;

import java.io.IOException;

public abstract class BaseSession implements Session {

	private Connection connection;

	@Override
	public final void init(Connection connection) {
		this.connection = connection;
	}

	@Override
	public final void close() throws IOException {
		connection.close();
	}

	@Override
	public final int send(byte[] data) throws IOException {
		return connection.send(data);
	}

	@Override
	public final void flush() throws IOException {
		connection.flush();
	}
}
