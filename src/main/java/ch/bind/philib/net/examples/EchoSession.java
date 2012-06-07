package ch.bind.philib.net.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.io.Ring;
import ch.bind.philib.net.PureSessionBase;
import ch.bind.philib.validation.SimpleValidation;

public class EchoSession extends PureSessionBase {

	private long lastInteractionNs;

	private final AtomicLong tx = new AtomicLong(0);

	private final AtomicLong rx = new AtomicLong(0);

	private final Ring<ByteBuffer> pendingWrites = new Ring<ByteBuffer>();

	@Override
	public void receive(ByteBuffer data) {
		lastInteractionNs = System.nanoTime();
		synchronized (pendingWrites) {
			try {
				rx.addAndGet(data.remaining());
				ByteBuffer pending = pendingWrites.pollNext(data);
				// int numGoodSends = 0;
				while (pending != null) {
					int rem = pending.remaining();
					SimpleValidation.isTrue(rem > 0);
					int num = send(pending);
					tx.addAndGet(num);
					if (num != rem) {
						// System.out.printf("cant echo back! only %d out of %d was sent.%n",
						// num, rem);
						pendingWrites.addFront(pending);
						break;
					}
					// numGoodSends++;
					releaseBuffer(pending);
					pending = pendingWrites.poll();
				}
				// if (numGoodSends > 1) {
				// System.out.println("good sends: " + numGoodSends);
				// }
			} catch (IOException e) {
				e.printStackTrace();
				try {
					close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
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

	public void printCacheStats() {
		System.out.println(getContext().getBufferCache().getCacheStats().toString());
	}

	public void forceWrite() throws IOException {
		synchronized (pendingWrites) {
			ByteBuffer data = pendingWrites.poll();
			if (data == null) {
				System.out.println("tried to riveve a connection, but there is nothing in the write queue");
				return;
			}
			int total = 0;
			while (data != null) {
				int rem = data.remaining();
				int num = send(data);
				if (num > 0) {
					total += num;
				}
				if (rem != num) {
					pendingWrites.addFront(data);
					data = null;
				} else {
					data = pendingWrites.poll();
				}
			}
			if (total > 0) {
				System.out.println("reviced connection by writing: " + total);
			}
		}
	}
}
