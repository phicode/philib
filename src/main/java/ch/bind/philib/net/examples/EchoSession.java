package ch.bind.philib.net.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.io.Ring;
import ch.bind.philib.net.PureSessionBase;

public class EchoSession extends PureSessionBase {

	private long lastInteractionNs;

	private final AtomicLong tx = new AtomicLong(0);

	private final AtomicLong rx = new AtomicLong(0);

	private final Ring<ByteBuffer> pendingWrites = new Ring<ByteBuffer>();

	@Override
	public void receive(ByteBuffer data) {
		try {
			rx.addAndGet(data.remaining());
//			if (pendingWrites.size() > 0) {
//				System.out.printf("pending writes: %d%n", pendingWrites.size());
//			}
			ByteBuffer pending = pendingWrites.poll();
			if (pending == null) {
				pending = data;
			} else {
				pendingWrites.addBack(data);
			}
			int rem = pending.remaining();
			int num = send(pending);
			tx.addAndGet(num);
			if (num != rem) {
				System.out.printf("cant echo back! only %d out of %d was sent.%n", num, rem);
				pendingWrites.addFront(pending);
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
		System.out.printf("closed() rx=%d, tx=%d%n", rx.get(), tx.get());
	}

	public long getLastInteractionNs() {
		return lastInteractionNs;
	}

	public long getRx() {
		return rx.get();
	}

	public long getTx() {
		return tx.get();
	}
}
