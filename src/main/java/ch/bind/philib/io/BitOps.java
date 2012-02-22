package ch.bind.philib.io;

public final class BitOps {

	private BitOps() {
	}

	public static int findLowestSetBitIdx64(final long v) {
		if (v == 0) {
			// no bits are set
			return -1;
		}
		long mask = 0x00000000FFFFFFFFL;
		int shift = 0;
		if ((v & mask) == 0) {
			shift += 32;
		}
		mask = (0xFFFFL << shift);
		if ((v & mask) == 0) {
			shift += 16;
		}
		mask = (0xFFL << shift);
		if ((v & mask) == 0) {
			shift += 8;
		}
		mask = (0xFL << shift);
		if ((v & mask) == 0) {
			shift += 4;
		}
		mask = (0x3L << shift);
		if ((v & mask) == 0) {
			shift += 2;
		}
		mask = (0x1L << shift);
		if ((v & mask) == 0) {
			shift += 1;
		}
		return shift;
	}
}
