package ch.bind.philib.io;

public class BufferPool extends ObjectPool<byte[]> {

	public static final int DEFAULT_BUFFER_SIZE = 8192;

	// 1 mb of buffers with the default buffer size of 8k
	public static final int DEFAULT_NUM_BUFFERS = 128;

	private final int bufSize;

	public BufferPool() {
		this(DEFAULT_BUFFER_SIZE, DEFAULT_NUM_BUFFERS);
	}

	public BufferPool(int bufferSize) {
		this(bufferSize, DEFAULT_NUM_BUFFERS);
	}

	public BufferPool(int bufferSize, int maxBuffers) {
		super(maxBuffers);
		this.bufSize = bufferSize;
	}

	@Override
	protected byte[] create() {
		return new byte[bufSize];
	}

	@Override
	public void release(byte[] buf) {
		// discard buffers which do not have the right size
		if (buf != null && buf.length == bufSize) {
			super.release(buf);
		}
	}
}
