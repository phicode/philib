package ch.bind.philib;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IntervalsTest {

	@Test
	public void chooseInterval1() {
		for (int i = 0; i <= 10; i++) {
			assertEquals(1, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval2() {
		for (int i = 11; i <= 20; i++) {
			assertEquals(2, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval5() {
		for (int i = 21; i <= 50; i++) {
			assertEquals(5, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval10() {
		for (int i = 51; i <= 100; i++) {
			assertEquals(10, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval25() {
		for (int i = 101; i <= 250; i++) {
			assertEquals(25, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval50() {
		for (int i = 251; i <= 500; i++) {
			assertEquals(50, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval100() {
		for (int i = 501; i <= 1000; i++) {
			assertEquals(100, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval250() {
		for (int i = 1001; i <= 2500; i++) {
			assertEquals(250, Intervals.chooseInterval(i, 10));
		}
	}

	@Test
	public void chooseInterval500() {
		for (int i = 2501; i <= 5000; i++) {
			assertEquals(500, Intervals.chooseInterval(i, 10));
		}
		assertEquals(1000, Intervals.chooseInterval(5001, 10));
	}
}
