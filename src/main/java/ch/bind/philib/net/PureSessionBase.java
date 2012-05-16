package ch.bind.philib.net;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class PureSessionBase implements PureSession {

	private Connection connection;
	
	@Override
	public final void init(Connection connection) {
		this.connection = connection;
		System.out.println("initialized!");
	}
	
	@Override
	public NetContext getContext() {
		return connection.getContext();
	}

	@Override
	public final void close() throws IOException {
		connection.close();
	}

	@Override
	public final int send(ByteBuffer data) throws IOException {
		return connection.send(data);
	}
}
