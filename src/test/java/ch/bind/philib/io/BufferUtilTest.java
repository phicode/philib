package ch.bind.philib.io;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.testng.annotations.Test;

import ch.bind.philib.lang.ArrayUtil;

public class BufferUtilTest {

	private static final byte[] abcde = "abcde".getBytes();

	private static final byte[] qwxyz = "qwxyz".getBytes();

	private static final byte[] both = ArrayUtil.concat(abcde, qwxyz);

	@Test
	public void appendWithEnoughSpace() {
		ByteBuffer dst = ByteBuffer.allocate(10);
		ByteBuffer src = ByteBuffer.allocate(5);

		// abcde_____
		dst.put(abcde);
		dst.flip();

		// qwxyz_____
		src.put(qwxyz);
		src.flip();

		ByteBuffer res = BufferUtil.append(dst, src);
		assertTrue(res == dst);
		verify(res, both);
	}

	@Test
	public void appendWithEnoughSpaceButCompactingNeeded() {
		ByteBuffer dst = ByteBuffer.allocate(10);
		ByteBuffer src = ByteBuffer.allocate(5);

		// __abcde___
		dst.position(2);
		dst.put(abcde);
		dst.position(2);
		dst.limit(7);

		// qwxyz_____
		src.put(qwxyz);
		src.flip();

		ByteBuffer res = BufferUtil.append(dst, src);
		assertTrue(res == dst);
		verify(res, both);
	}

	@Test
	public void appendNotEnoughSpace() {
		ByteBuffer dst1 = ByteBuffer.allocate(8);
		ByteBuffer dst2 = ByteBuffer.allocateDirect(8);
		ByteBuffer src = ByteBuffer.allocate(5);

		// __abcde__
		dst1.position(2);
		dst1.put(abcde);
		dst1.position(2);
		dst1.limit(7);
		dst2.position(2);
		dst2.put(abcde);
		dst2.position(2);
		dst2.limit(7);

		// qwxyz_____
		src.put(qwxyz);
		src.flip();

		ByteBuffer res1 = BufferUtil.append(dst1, src);
		src.flip();
		ByteBuffer res2 = BufferUtil.append(dst2, src);
		assertTrue(res1 != dst1);
		assertTrue(res2 != dst2);
		assertFalse(res1.isDirect());
		assertTrue(res2.isDirect());

		verify(res1, both);
		verify(res2, both);
	}

	@Test
	public void appendToEmpty() {
		ByteBuffer dst = ByteBuffer.allocate(8);
		ByteBuffer src = ByteBuffer.allocate(5);

		// ________
		dst.position(0);
		dst.limit(0);

		// qwxyz_____
		src.put(qwxyz);
		src.flip();

		ByteBuffer res = BufferUtil.append(dst, src);
		assertTrue(res == dst);

		verify(res, qwxyz);
	}

	private void verify(ByteBuffer res, byte[] expected) {
		assertEquals(res.remaining(), expected.length);
		byte[] verify = new byte[expected.length];
		res.get(verify);
		assertEquals(verify, expected);
	}

}
