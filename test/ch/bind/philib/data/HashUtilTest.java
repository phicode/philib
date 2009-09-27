package ch.bind.philib.data;

import static org.junit.Assert.*;
import org.junit.Test;

public class HashUtilTest {

	@Test
	public void testByteHashes() {
		int hash = 0;
		byte value = 0;
		int expected = 31;

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

		for (int i = 1; i <= 255; i++) {
			value = (byte) i;
			hash = HashUtil.nextHash(hash, value);
			expected = expected * 31 + value;
			assertEquals(expected, hash);
		}
	}
}
