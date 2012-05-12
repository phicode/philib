package ch.bind.philib.net;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

public abstract class AConnection implements Connection {

	private SelectableChannel channel;
	
	@Override
	public final void send(byte[] data) throws IOException {
		channel.
		
		// TODO Auto-generated method stub

	}

	@Override
	public final void close() throws IOException {
		// TODO Auto-generated method stub

	}
}
