package ch.bind.philib.data;

import static org.junit.Assert.*;
import org.junit.Test;

public class HashUtilTest {

	@Test
	public void testObjectHashes() {
		int hash = 0;
		Integer value = 0;
		int expected = 17 * 31;

		hash = HashUtil.nextHash(hash, value);
		assertEquals(expected, hash);

		value = (int) 4294967295L;// 2^32-1
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 1;
		assertEquals(expected, hash);

		value = (int) 2147483647L; // 2^31-1
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 2147483647;
		assertEquals(expected, hash);

		value = (int) 2147483648L; // 2^31
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + -2147483648;
		assertEquals(expected, hash);

		value = null;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31;
		assertEquals(expected, hash);
	}

	@Test
	public void testBooleanHashes() {
		int hash = 0;
		boolean value = false;
		int expected = 17 * 31;

		hash = HashUtil.nextHash(hash, value);
		assertEquals(expected, hash);

		value = true;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 1;
		assertEquals(expected, hash);

		value = true;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 1;
		assertEquals(expected, hash);

		value = false;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 0;
		assertEquals(expected, hash);

		value = false;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 0;
		assertEquals(expected, hash);
	}

	@Test
	public void testByteHashes() {
		int hash = 0;
		byte value = 0;
		int expected = 17 * 31;

		hash = HashUtil.nextHash(hash, value);
		assertEquals(expected, hash);

		value = (byte) 254;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 2;
		assertEquals(expected, hash);

		value = (byte) 127;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 127;
		assertEquals(expected, hash);

		value = (byte) 128;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 128;
		assertEquals(expected, hash);

		for (int i = 1; i < 256; i++) {
			value = (byte) i;
			hash = HashUtil.nextHash(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}

	@Test
	public void testShortHashes() {
		int hash = 0;
		short value = 0;
		int expected = 17 * 31;

		hash = HashUtil.nextHash(hash, value);
		assertEquals(expected, hash);

		value = (short) 65534;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 2;
		assertEquals(expected, hash);

		value = (short) 32767;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 32767;
		assertEquals(expected, hash);

		value = (short) 32768;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 32768;
		assertEquals(expected, hash);

		for (int i = 1; i < 65536; i++) {
			value = (short) i;
			hash = HashUtil.nextHash(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}

	@Test
	public void testCharHashes() {
		int hash = 0;
		char value = 0;
		int expected = 17 * 31;

		hash = HashUtil.nextHash(hash, value);
		assertEquals(expected, hash);

		value = (char) 65534;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 65534;
		assertEquals(expected, hash);

		value = (char) 32767;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 32767;
		assertEquals(expected, hash);

		value = (char) 32768;
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 32768;
		assertEquals(expected, hash);

		for (int i = 1; i < 65536; i++) {
			value = (char) i;
			hash = HashUtil.nextHash(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}

	@Test
	public void testIntHashes() {
		int hash = 0;
		int value = 0;
		int expected = 17 * 31;

		hash = HashUtil.nextHash(hash, value);
		assertEquals(expected, hash);

		value = (int) 4294967295L;// 2^32-1
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 - 1;
		assertEquals(expected, hash);

		value = (int) 2147483647L; // 2^31-1
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + 2147483647;
		assertEquals(expected, hash);

		value = (int) 2147483648L; // 2^31
		hash = HashUtil.nextHash(hash, value);
		expected = expected * 31 + -2147483648;
		assertEquals(expected, hash);
	}

	@Test
	public void testLongHashes() {
		int hash = 0;
		long value = 0;
		int expected = 17 * 31;

		hash = HashUtil.nextHash(hash, value);
		assertEquals(expected, hash);

		value = 9223372036854775807L; // 2^63-1
		hash = HashUtil.nextHash(hash, value);
		int masked = (int) (value >>> 32 ^ value);
		expected = expected * 31 + masked;
		assertEquals(expected, hash);

		value++; // 2^31
		hash = HashUtil.nextHash(hash, value);
		masked = (int) (value >>> 32 ^ value);
		expected = expected * 31 + masked;
		assertEquals(expected, hash);
	}

	@Test
	public void testFloatHashes() {
		int hash = 0;
		float value = 0;
		int expected = HashUtil.nextHash(0, Float.floatToIntBits(value));

		hash = HashUtil.nextHash(hash, value);
		assertEquals(expected, hash);

		value = Float.MIN_VALUE;
		hash = HashUtil.nextHash(hash, value);
		expected = HashUtil.nextHash(expected, Float.floatToIntBits(value));
		assertEquals(expected, hash);

		value = Float.MAX_VALUE;
		hash = HashUtil.nextHash(hash, value);
		expected = HashUtil.nextHash(expected, Float.floatToIntBits(value));
		assertEquals(expected, hash);
	}

	@Test
	public void testDoubleHashes() {
		int hash = 0;
		double value = 0;
		int expected = HashUtil.nextHash(0, Double.doubleToLongBits(value));

		hash = HashUtil.nextHash(hash, value);
		assertEquals(expected, hash);

		value = Double.MIN_VALUE;
		hash = HashUtil.nextHash(hash, value);
		expected = HashUtil.nextHash(expected, Double.doubleToLongBits(value));
		assertEquals(expected, hash);

		value = Double.MAX_VALUE;
		hash = HashUtil.nextHash(hash, value);
		expected = HashUtil.nextHash(expected, Double.doubleToLongBits(value));
		assertEquals(expected, hash);
	}
}
