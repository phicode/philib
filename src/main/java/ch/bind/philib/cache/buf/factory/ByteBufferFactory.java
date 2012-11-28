package ch.bind.philib.cache.buf.factory;

import java.nio.ByteBuffer;

import ch.bind.philib.lang.ArrayUtil;

public final class ByteBufferFactory implements BufferFactory<ByteBuffer> {

	public static final boolean DEFAULT_DIRECT_BUFFER = true;

	private final int bufferSize;

	private final boolean directBuffer;

	public ByteBufferFactory(int bufferSize) {
		this(bufferSize, DEFAULT_DIRECT_BUFFER);
	}

	public ByteBufferFactory(int bufferSize, boolean directBuffer) {
		this.bufferSize = bufferSize;
		this.directBuffer = directBuffer;
	}

	@Override
	public ByteBuffer create() {
		if (directBuffer) {
			return ByteBuffer.allocateDirect(bufferSize);
		}
		else {
			return ByteBuffer.allocate(bufferSize);
		}
	}

	@Override
	public boolean prepareForReuse(final ByteBuffer e) {
		if (e.capacity() == bufferSize && e.isDirect() == directBuffer) {
			ArrayUtil.memsetZero(e);
			return true;
		}
		return false;
	}

}
