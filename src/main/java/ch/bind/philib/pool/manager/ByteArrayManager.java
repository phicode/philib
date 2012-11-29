package ch.bind.philib.pool.manager;

import ch.bind.philib.lang.ArrayUtil;

public final class ByteArrayManager implements ObjectManager<byte[]> {

	private final int bufferSize;

	public ByteArrayManager(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@Override
	public byte[] create() {
		return new byte[bufferSize];
	}

	@Override
	public boolean prepareForRecycle(byte[] buf) {
		if (buf.length == bufferSize) {
			ArrayUtil.memsetZero(buf);
			return true;
		}
		return false;
	}

	@Override
	public void release(byte[] buf) {
		// the gc will take care of this :)
	}

	@Override
	public boolean canReuse(byte[] buf) {
		return true;
	}
}
