package ch.bind.philib.lang;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static ch.bind.philib.lang.ToString.*;

public class ToStringTest {

	@Test
	public void toStrStart() {
		assertEquals(start(this).toString(), "ToStringTest[");
	}

	@Test
	public void toStrEnd() {
		assertEquals(end(start(this)), "ToStringTest[]");
	}

	@Test
	public void toStrObj() {
		assertEquals( //
		        end(append(first(start(this), "a", "1"), "b", "2")), //
		        "ToStringTest[a=1, b=2]");
	}

	@Test
	public void toStrObjNoName() {
		assertEquals( //
		        end(append(first(start(this), "1"), "2")), //
		        "ToStringTest[1, 2]");
	}

	@Test
	public void toStrChar() {
		char a = 'a', b = 'b';
		assertEquals( //
		        end(append(first(start(this), "a", a), "b", b)), //
		        "ToStringTest[a=a, b=b]");
	}

	@Test
	public void toStrCharNoName() {
		char a = 'a', b = 'b';
		assertEquals( //
		        end(append(first(start(this), a), b)), //
		        "ToStringTest[a, b]");
	}

	@Test
	public void toStrBool() {
		boolean a = true, b = false;
		assertEquals( //
		        end(append(first(start(this), "a", a), "b", b)), //
		        "ToStringTest[a=true, b=false]");
	}

	@Test
	public void toStrBoolNoName() {
		boolean a = true, b = false;
		assertEquals( //
		        end(append(first(start(this), a), b)), //
		        "ToStringTest[true, false]");
	}

	@Test
	public void toStrInt() {
		int a = Integer.MAX_VALUE, b = Integer.MIN_VALUE;
		assertEquals( //
		        end(append(first(start(this), "a", a), "b", b)), //
		        "ToStringTest[a=2147483647, b=-2147483648]");
	}

	@Test
	public void toStrIntNoName() {
		int a = Integer.MAX_VALUE, b = Integer.MIN_VALUE;
		assertEquals( //
		        end(append(first(start(this), a), b)), //
		        "ToStringTest[2147483647, -2147483648]");
	}

	@Test
	public void toStrLong() {
		long a = Long.MAX_VALUE, b = Long.MIN_VALUE;
		assertEquals( //
		        end(append(first(start(this), "a", a), "b", b)), //
		        "ToStringTest[a=9223372036854775807, b=-9223372036854775808]");
	}

	@Test
	public void toStrLongNoName() {
		long a = Long.MAX_VALUE, b = Long.MIN_VALUE;
		assertEquals( //
		        end(append(first(start(this), a), b)), //
		        "ToStringTest[9223372036854775807, -9223372036854775808]");
	}

	@Test
	public void toStrFloat() {
		float a = Float.MAX_VALUE, b = Float.MIN_VALUE;
		assertEquals( //
		        end(append(first(start(this), "a", a), "b", b)), //
		        "ToStringTest[a=3.4028235E38, b=1.4E-45]");
	}

	@Test
	public void toStrFloatNoName() {
		float a = Float.MAX_VALUE, b = Float.MIN_VALUE;
		assertEquals( //
		        end(append(first(start(this), a), b)), //
		        "ToStringTest[3.4028235E38, 1.4E-45]");
	}

	@Test
	public void toStrFloatSpecial() {
		float a = Float.NaN, b = -0f, c = 0f, d = Float.NEGATIVE_INFINITY, e = Float.POSITIVE_INFINITY;
		assertEquals( //
		        end(append(append(append(append(first(start(this), a), b), c), d), e)), //
		        "ToStringTest[NaN, -0.0, 0.0, -Infinity, Infinity]");
	}

	@Test
	public void toStrDouble() {
		double a = Double.MAX_VALUE, b = Double.MIN_VALUE;
		assertEquals( //
		        end(append(first(start(this), "a", a), "b", b)), //
		        "ToStringTest[a=1.7976931348623157E308, b=4.9E-324]");
	}

	@Test
	public void toStrDoubleNoName() {
		double a = Double.MAX_VALUE, b = Double.MIN_VALUE;
		assertEquals( //
		        end(append(first(start(this), a), b)), //
		        "ToStringTest[1.7976931348623157E308, 4.9E-324]");
	}

	@Test
	public void toStrDoubleSpecial() {
		double a = Double.NaN, b = -0d, c = 0d, d = Double.NEGATIVE_INFINITY, e = Double.POSITIVE_INFINITY;
		assertEquals( //
		        end(append(append(append(append(first(start(this), a), b), c), d), e)), //
		        "ToStringTest[NaN, -0.0, 0.0, -Infinity, Infinity]");
	}
}
