package ch.bind.philib.io;

import java.nio.ByteBuffer;

public final class BlockingByteBufferQueue {

	private Ring<ByteBuffer> ring = new Ring<ByteBuffer>();
	private final int maxSizeBeforeBlocking;
	private int size;

	public BlockingByteBufferQueue(int maxSizeBeforeBlocking) {
		this.maxSizeBeforeBlocking = maxSizeBeforeBlocking;
	}

	public synchronized void addOrWait(ByteBuffer data) throws InterruptedException {
		final int rem = data.remaining();
		while ((size + rem) > maxSizeBeforeBlocking) {
			wait();
		}
		size += rem;
		ring.addBack(data);
	}

	public synchronized void addFront(ByteBuffer data) {
		size += data.remaining();
		ring.addFront(data);
	}

	public synchronized ByteBuffer poll() {
		ByteBuffer data = ring.poll();
		if (data != null) {
			size -= data.remaining();
			notifyAll();
		}
		return data;
	}
}
