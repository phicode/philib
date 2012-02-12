package ch.bind.philib.io;

public class BufferPool {

	public static final int DEFAULT_BUFFER_SIZE = 8192;

	private final int bufferSize;

	public BufferPool(int bufferSize) {
		super();
		this.bufferSize = bufferSize;
	}

	byte[] getBuffer() {

	}

	release(byte[] buf) {

	}
}
