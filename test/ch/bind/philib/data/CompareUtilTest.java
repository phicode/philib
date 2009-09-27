package ch.bind.philib.data;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class CompareUtilTest {

	@Test
	public void equalityObjObj() {
		Object a = "a";
		Object b = "b";

		boolean eq = CompareUtil.equality(a, b);
		assertFalse(eq);

		a = "b";
		eq = CompareUtil.equality(a, b);
		assertTrue(eq);

		b = "a";
		eq = CompareUtil.equality(a, b);
		assertFalse(eq);
	}

	@Test
	public void equalityNullNull() {
		Object a = null;
		Object b = null;

		boolean eq = CompareUtil.equality(a, b);
		assertTrue(eq);
	}

	@Test
	public void equalityObjNull() {
		Object a = "a";
		Object b = null;

		boolean eq = CompareUtil.equality(a, b);
		assertFalse(eq);
	}

	@Test
	public void equalityNullObj() {
		Object a = null;
		Object b = "b";

		boolean eq = CompareUtil.equality(a, b);
		assertFalse(eq);
	}
}
