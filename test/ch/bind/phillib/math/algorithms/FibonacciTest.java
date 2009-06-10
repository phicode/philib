package ch.bind.phillib.math.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.bind.philib.math.algorithms.Fibonacci;

public class FibonacciTest {

	private Fibonacci fib;

	@Before
	public void setup() {
		this.fib = new Fibonacci();
	}

	@After
	public void tearDown() {
		this.fib = null;
	}

	@Test
	public void testNextFib() {
		assertEquals(1, fib.nextFib());
		assertEquals(2, fib.nextFib());
		assertEquals(3, fib.nextFib());
		assertEquals(5, fib.nextFib());
		assertEquals(8, fib.nextFib());
		assertEquals(13, fib.nextFib());
		assertEquals(21, fib.nextFib());
		assertEquals(34, fib.nextFib());
		assertEquals(55, fib.nextFib());
		assertEquals(89, fib.nextFib());
		assertEquals(144, fib.nextFib());
		assertEquals(233, fib.nextFib());
		assertEquals(377, fib.nextFib());
		assertEquals(610, fib.nextFib());
		assertEquals(987, fib.nextFib());
		assertEquals(1597, fib.nextFib());
	}
}
