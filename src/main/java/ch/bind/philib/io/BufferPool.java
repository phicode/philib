package ch.bind.philib.io;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class BufferPool extends ObjPool<byte[]> {

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

	private final AtomicLong creates=new AtomicLong();
	@Override
	protected byte[] create() {
		creates.incrementAndGet();
		return new byte[bufSize];
	}

	public long getNumCreates() {
		return creates.get();
	}
	@Override
	public void release(byte[] buf) {
		// discard buffers which do not have the right size
		if (buf != null && buf.length == bufSize) {
			super.release(buf);
		}
	}
}
