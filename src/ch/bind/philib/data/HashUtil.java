package ch.bind.philib.data;

public final class HashUtil {

	private HashUtil() {
	}

	private static final int HASH_PRIME = 31;

	public static final int nextHash(int hash, final Object obj) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + ((obj == null) ? 0 : obj.hashCode());
	}

	public static final int nextHash(int hash, final byte value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + (int)value;
	}

	public static final int nextHash(int hash, final char value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + value;
	}

	public static final int nextHash(int hash, final short value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + value;
	}

	public static final int nextHash(int hash, final int value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + value;
	}

	public static final int nextHash(int hash, final float value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + Float.valueOf(value).hashCode();
	}

	public static final int nextHash(int hash, final double value) {
		if (hash == 0)
			hash = 1;
		return hash * HASH_PRIME + Double.valueOf(value).hashCode();
	}
}
