package ch.bind.philib.io;

public final class BufferQueue {

	private final long maxBufSize;

	private final Ring<byte[]> ring = new Ring<byte[]>();

	private long curBufSize;

	public BufferQueue() {
		this(Long.MAX_VALUE);
	}

	public BufferQueue(long maxBufSize) {
		this.maxBufSize = maxBufSize;
	}

	public boolean offer(byte[] data) {
		if (data == null || data.length == 0) {
			return true;
		}
		else {
			long newSize = data.length + curBufSize;
			if (newSize > maxBufSize) {
				return false;
			}
			else {
				ring.add(data);
				curBufSize = newSize;
				return true;
			}
		}
	}

	public byte[] poll() {
		if (curBufSize == 0) {
			return null;
		}
		else {
			byte[] value = ring.get();
			assert (value != null);
			curBufSize -= value.length;
			return value;
		}
	}

	public long size() {
		return curBufSize;
	}

	public boolean isEmpty() {
		return curBufSize == 0;
	}
}
