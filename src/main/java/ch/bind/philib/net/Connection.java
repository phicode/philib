package ch.bind.philib.net;

import java.io.Closeable;
import java.io.IOException;

public interface Connection extends Closeable, Selectable {

	/**
	 * 
	 * @param data
	 * @return {@code true} if closed, {@code false} otherwise.
	 * @throws IOException
	 */
	void send(byte[] data) throws IOException;

}
