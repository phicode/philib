package ch.bind.philib.cache.buf.factory;

import ch.bind.philib.lang.ArrayUtil;

public final class ByteArrayFactory implements BufferFactory<byte[]> {

	private final int bufferSize;

	public ByteArrayFactory(int bufSize) {
		this.bufferSize = bufSize;
	}

	@Override
	public byte[] create() {
		return new byte[bufferSize];
	}

	@Override
	public boolean prepareForReuse(byte[] e) {
		if (e.length == bufferSize) {
			ArrayUtil.memsetZero(e);
			return true;
		}
		return false;
	}
}
