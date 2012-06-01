package ch.bind.philib.lang;

import ch.bind.philib.io.BitOps;
import ch.bind.philib.io.EndianConverter;

public final class MurmurHash {

	private MurmurHash() {
	}

	private static final int MURMUR2_32_M = 0x5bd1e995;

	private static final int MURMUR2_32_R = 24;

	private static final int MURMUR2_32_SEED = 0x9747b28c;

	// private static final int MURMUR3_32_SEED = ;

	private static final int MURMUR3_32_C1 = 0xcc9e2d51;

	private static final int MURMUR3_32_C2 = 0x1b873593;

	public static final int murmur2(byte[] key) {
		final int len = key.length;
		final int limitOffset = len & 0xFFFFFFFC;

		int off = 0;
		int hash = MURMUR2_32_SEED ^ len;

		while (off < limitOffset) {
			int k = EndianConverter.decodeInt32LE(key, off);
			off += 4;

			k *= MURMUR2_32_M;
			k ^= (k >>> MURMUR2_32_R);
			k *= MURMUR2_32_M;

			hash *= MURMUR2_32_M;
			hash ^= k;
		}

		switch (len & 0x3) {
		case 3:
			hash ^= ((key[off + 2] & 0xFF) << 16);
		case 2:
			hash ^= ((key[off + 1] & 0xFF) << 8);
		case 1:
			hash ^= (key[off] & 0xFF);

			hash *= MURMUR2_32_M;
		}

		return murmur2_finalize(hash);
	}

	private static final int murmur2_finalize(int hash) {
		hash ^= (hash >>> 13);
		hash *= MURMUR2_32_M;
		hash ^= (hash >>> 15);
		return hash;
	}

	public static final int murmur3(byte[] key) {
		return murmur3(key, MURMUR2_32_SEED);
	}

	public static final int murmur3(byte[] key, int seed) {
		final int len = key.length;
		final int limitOffset = len & 0xFFFFFFFC;

		int off = 0;
		int hash = seed;

		while (off < limitOffset) {
			int k = EndianConverter.decodeInt32LE(key, off);
			off += 4;

			// k *= MURMUR3_32_C1;
			// k = BitOps.rotl32(k, 15);
			// k *= MURMUR3_32_C2;
			// hash ^= k;
			hash ^= murmur3_round32(k);
			hash = BitOps.rotl32(hash, 13);
			hash = (hash * 5) + 0xe6546b64;
		}

		int k = 0;
		switch (len & 0x3) {
		case 3:
			k ^= ((key[off + 2] & 0xFF) << 16);
		case 2:
			k ^= ((key[off + 1] & 0xFF) << 8);
		case 1:
			k ^= (key[off] & 0xFF);

			// k *= MURMUR3_32_C1;
			// k = BitOps.rotl32(k, 15);
			// k *= MURMUR3_32_C2;
			// hash ^= k;
			hash ^= murmur3_round32(k);
		}

		hash ^= len;
		hash = murmur3_fmix32(hash);
		return hash;
	}

	private static final int murmur3_round32(int k) {
		k *= MURMUR3_32_C1;
		k = BitOps.rotl32(k, 15);
		k *= MURMUR3_32_C2;
		return k;
	}

	private static final int murmur3_fmix32(int hash) {
		hash ^= hash >>> 16;
		hash *= 0x85ebca6b;
		hash ^= hash >>> 13;
		hash *= 0xc2b2ae35;
		hash ^= hash >>> 16;
		return hash;
	}

	// private static final long murmur3_fmix64(long k) {
	// k ^= k >>> 33;
	// k *= 0xff51afd7ed558ccdL;
	// k ^= k >>> 33;
	// k *= 0xc4ceb9fe1a85ec53L;
	// k ^= k >>> 33;
	// return k;
	// }

	public static final long optimize() {
		byte[] b = {
				1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
		final long s = System.nanoTime();
		for (int i = 0; i < 12000; i++) {
			murmur2(b);
			murmur3(b);
		}
		return (System.nanoTime() - s);
	}
}
