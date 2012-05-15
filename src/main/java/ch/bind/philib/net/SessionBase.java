package ch.bind.philib.net;

import java.io.IOException;

public abstract class SessionBase implements Session {

	private Connection connection;

	@Override
	public final void init(Connection connection) {
		this.connection = connection;
		System.out.println("initialized!");
	}

	@Override
	public final void close() throws IOException {
		connection.close();
	}

	@Override
	public final int send(byte[] data) throws IOException {
		return connection.send(data);
	}
}
