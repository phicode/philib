package ch.bind.philib.net.conn;

import java.net.ConnectException;

public class ConnectTimeoutException extends ConnectException {

	private static final long serialVersionUID = -4158808136333056309L;

	public ConnectTimeoutException(String message) {
		super(message);
	}
}
