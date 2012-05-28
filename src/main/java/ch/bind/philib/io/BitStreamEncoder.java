package ch.bind.philib.io;

import ch.bind.philib.lang.ArrayUtil;

public class BitStreamEncoder {

	private static final int DEFAULT_INITIAL_SIZE = 16;

	private byte[] encoded = new byte[DEFAULT_INITIAL_SIZE];

	private int encodedSize;

	private long active;

	private int bitsLeft;

	public BitStreamEncoder() {
		// this.data = new byte[DEFAULT_INITIAL_SIZE];
		// this.capacity = DEFAULT_INITIAL_SIZE * 8;
		bitsLeft = 64;
	}

	public void writeByte(int value) {
		if (bitsLeft < 8) {
			// flush();
		}
		// active = ((active << 8) | (value & MASK[8]));
	}

	private void ensureSizeBytes(int num) {
		int len = encoded.length;
		int rem = len - encodedSize;
		if (rem < num) {
			byte[] enc = new byte[len * 2];
			encoded = ac(encoded, enc, len);
		}
	}

	private static byte[] ac(byte[] src, byte[] dst, int len) {
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}

	private void finishEncode() {

	}
}
