package ch.bind.philib.pool.manager;

import java.nio.ByteBuffer;

import ch.bind.philib.lang.ArrayUtil;

public final class ByteBufferManager implements ObjectManager<ByteBuffer> {

	public static final boolean DEFAULT_DIRECT_BUFFER = true;

	private final int bufferSize;

	private final boolean directBuffer;

	public ByteBufferManager(int bufferSize) {
		this(bufferSize, DEFAULT_DIRECT_BUFFER);
	}

	public ByteBufferManager(int bufferSize, boolean directBuffer) {
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
	public boolean prepareForRecycle(final ByteBuffer buf) {
		if (buf.capacity() == bufferSize && buf.isDirect() == directBuffer) {
			ArrayUtil.memsetZero(buf);
			return true;
		}
		return false;
	}

	@Override
	public void release(ByteBuffer buf) {
		// the gc will take care of this :)
	}

	@Override
	public boolean canReuse(ByteBuffer buf) {
		return true;
	}
}
