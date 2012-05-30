package ch.bind.philib.lang;

import ch.bind.philib.io.EndianConverter;

public final class MurmurHash {

	private MurmurHash() {
	}

	private static final int MURMUR2_32_M = 0x5bd1e995;

	private static final int MURMUR2_32_R = 24;

	private static final int MURMUR2_32_SEED = 0x9747b28c;

	public static final int murmur2(byte[] key) {
		final int len = key.length;
		int hash = MURMUR2_32_SEED ^ len;

		// int rem = len;
		final int lenMod4 = len & 0x3;
		final int limitOffset = len & 0xFFFFFFFC;
		int off = 0;
		while (off < limitOffset) {
			int k = EndianConverter.decodeInt32LE(key, off);
			off += 4;

			k = k * MURMUR2_32_M;
			k = k ^ (k >>> MURMUR2_32_R);
			k = k * MURMUR2_32_M;

			hash = hash * MURMUR2_32_M;
			hash = hash ^ k;
		}

		// swap endian order of remaining bytes
		if (lenMod4 > 0) {
			switch (lenMod4) {
			case 3:
				hash ^= ((key[off + 2] & 0xFF) << 16);
			case 2:
				hash ^= ((key[off + 1] & 0xFF) << 8);
			case 1:
				hash ^= (key[off] & 0xFF);
			}
			hash = hash * MURMUR2_32_M;
		}

		 return murmur2_finalize(hash);
//		hash = hash ^ (hash >>> 13);
//		hash = hash * MURMUR2_32_M;
//		hash = hash ^ (hash >>> 15);
//		return hash;
	}

	private static final int murmur2_finalize(int hash) {
		hash = hash ^ (hash >>> 13);
		hash = hash * MURMUR2_32_M;
		hash = hash ^ (hash >>> 15);
		return hash;
	}
}
