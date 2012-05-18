package ch.bind.philib.net.examples;

import java.io.IOException;
import java.nio.ByteBuffer;

import ch.bind.philib.net.PureSessionBase;

public class EchoSession extends PureSessionBase {

	private long lastInteractionNs;

	// private boolean closed;

	private long numEchoed;

	@Override
	public void receive(ByteBuffer data) {
		try {
			int received = data.remaining();
			int num = send(data);
			numEchoed += num;
			if (num != received) {
				System.out.printf("cant echo back! only %d out of %d was sent.%n", num, received);
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		lastInteractionNs = System.nanoTime();
	}

	@Override
	public void closed() {
		// this.closed = true;
		System.out.println("closed() numEchoed=" + numEchoed);
	}

	public long getLastInteractionNs() {
		return lastInteractionNs;
	}

	public long getNumEchoed() {
		return numEchoed;
	}
}
