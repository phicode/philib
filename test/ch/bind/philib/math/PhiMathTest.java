package ch.bind.philib.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.bind.philib.math.PhiMath;

public class PhiMathTest {

	@Test
	public void sumOfRange1() {
		assertEquals(0, PhiMath.sumOfRange(-9999));
		assertEquals(0, PhiMath.sumOfRange(-1));
		assertEquals(1, PhiMath.sumOfRange(1));
		assertEquals(3, PhiMath.sumOfRange(2));
		assertEquals(6, PhiMath.sumOfRange(3));
		assertEquals(5050, PhiMath.sumOfRange(100));
	}

	@Test
	public void sumOfRange2() {
		assertEquals(0, PhiMath.sumOfRange(10, 9));
		assertEquals(0, PhiMath.sumOfRange(1, 0));
		assertEquals(0, PhiMath.sumOfRange(-9999, -9998));
		assertEquals(0, PhiMath.sumOfRange(-1, 0));
		assertEquals(1, PhiMath.sumOfRange(1, 1));
		assertEquals(3, PhiMath.sumOfRange(1, 2));
		assertEquals(2, PhiMath.sumOfRange(2, 2));
		assertEquals(6, PhiMath.sumOfRange(1, 3));
		assertEquals(5, PhiMath.sumOfRange(2, 3));
		assertEquals(3, PhiMath.sumOfRange(3, 3));
		assertEquals(5050, PhiMath.sumOfRange(1, 100));
		assertEquals(5049, PhiMath.sumOfRange(2, 100));
		assertEquals(5047, PhiMath.sumOfRange(3, 100));
		assertEquals(5044, PhiMath.sumOfRange(4, 100));
		assertEquals(5040, PhiMath.sumOfRange(5, 100));
	}
}
