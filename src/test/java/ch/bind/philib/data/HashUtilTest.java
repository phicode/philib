package ch.bind.philib.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HashUtilTest {

	@Test
	public void testObjectHashes() {
		Integer value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.start(value);
		assertEquals(expected, hash);

		value = (int) 4294967295L; // 2^32-1
		hash = HashUtil.next(hash, value);
		expected = expected * 31 - 1;
		assertEquals(expected, hash);

		value = (int) 2147483647L; // 2^31-1
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 2147483647;
		assertEquals(expected, hash);

		value = (int) 2147483648L; // 2^31
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + -2147483648;
		assertEquals(expected, hash);

		value = null;
		hash = HashUtil.next(hash, value);
		expected = expected * 31;
		assertEquals(expected, hash);
	}

	@Test
	public void testBooleanHashes() {
		boolean value = false;
		int expected = 17 * 31;

		int hash = HashUtil.start(value);
		assertEquals(expected, hash);

		value = true;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 1;
		assertEquals(expected, hash);

		value = true;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 1;
		assertEquals(expected, hash);

		value = false;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 0;
		assertEquals(expected, hash);

		value = false;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 0;
		assertEquals(expected, hash);
	}

	@Test
	public void testByteHashes() {
		byte value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.start(value);
		assertEquals(expected, hash);

		value = (byte) 254;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 - 2;
		assertEquals(expected, hash);

		value = (byte) 127;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 127;
		assertEquals(expected, hash);

		value = (byte) 128;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 - 128;
		assertEquals(expected, hash);

		for (int i = 1; i < 256; i++) {
			value = (byte) i;
			hash = HashUtil.next(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}

	@Test
	public void testShortHashes() {
		short value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.start(value);
		assertEquals(expected, hash);

		value = (short) 65534;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 - 2;
		assertEquals(expected, hash);

		value = (short) 32767;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 32767;
		assertEquals(expected, hash);

		value = (short) 32768;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 - 32768;
		assertEquals(expected, hash);

		for (int i = 1; i < 65536; i++) {
			value = (short) i;
			hash = HashUtil.next(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}

	@Test
	public void testCharHashes() {
		char value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.start(value);
		assertEquals(expected, hash);

		value = (char) 65534;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 65534;
		assertEquals(expected, hash);

		value = (char) 32767;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 32767;
		assertEquals(expected, hash);

		value = (char) 32768;
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 32768;
		assertEquals(expected, hash);

		for (int i = 1; i < 65536; i++) {
			value = (char) i;
			hash = HashUtil.next(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}

	@Test
	public void testIntHashes() {
		int value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.start(value);
		assertEquals(expected, hash);

		value = (int) 4294967295L; // 2^32-1
		hash = HashUtil.next(hash, value);
		expected = expected * 31 - 1;
		assertEquals(expected, hash);

		value = (int) 2147483647L; // 2^31-1
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + 2147483647;
		assertEquals(expected, hash);

		value = (int) 2147483648L; // 2^31
		hash = HashUtil.next(hash, value);
		expected = expected * 31 + -2147483648;
		assertEquals(expected, hash);
	}

	@Test
	public void testLongHashes() {
		long value = 0;
		int expected = 17 * 31;

		int hash = HashUtil.start(value);
		assertEquals(expected, hash);

		value = 9223372036854775807L; // 2^63-1
		hash = HashUtil.next(hash, value);
		int masked = (int) (value >>> 32 ^ value);
		expected = expected * 31 + masked;
		assertEquals(expected, hash);

		value++; // 2^31
		hash = HashUtil.next(hash, value);
		masked = (int) (value >>> 32 ^ value);
		expected = expected * 31 + masked;
		assertEquals(expected, hash);
	}

	@Test
	public void testFloatHashes() {
		float value = 0;
		int expected = HashUtil.start(Float.floatToIntBits(value));

		int hash = HashUtil.start(value);
		assertEquals(expected, hash);

		value = Float.MIN_VALUE;
		hash = HashUtil.next(hash, value);
		expected = HashUtil.next(expected, Float.floatToIntBits(value));
		assertEquals(expected, hash);

		value = Float.MAX_VALUE;
		hash = HashUtil.next(hash, value);
		expected = HashUtil.next(expected, Float.floatToIntBits(value));
		assertEquals(expected, hash);
	}

	@Test
	public void testDoubleHashes() {
		double value = 0;
		int expected = HashUtil.start(Double.doubleToLongBits(value));

		int hash = HashUtil.start(value);
		assertEquals(expected, hash);

		value = Double.MIN_VALUE;
		hash = HashUtil.next(hash, value);
		expected = HashUtil.next(expected, Double.doubleToLongBits(value));
		assertEquals(expected, hash);

		value = Double.MAX_VALUE;
		hash = HashUtil.next(hash, value);
		expected = HashUtil.next(expected, Double.doubleToLongBits(value));
		assertEquals(expected, hash);
	}
}
