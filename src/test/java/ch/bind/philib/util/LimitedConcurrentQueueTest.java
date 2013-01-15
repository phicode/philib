package ch.bind.philib.util;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class LimitedConcurrentQueueTest {

	@Test
	public void offer() {
		LimitedConcurrentQueue<Integer> lcq = new LimitedConcurrentQueue<Integer>(2);
		assertEquals(lcq.getLimit(), 2);
		assertTrue(lcq.offer(1));
		assertTrue(lcq.offer(2));
		assertFalse(lcq.offer(3));
	}

	@Test
	public void poll() {
		LimitedConcurrentQueue<Integer> lcq = new LimitedConcurrentQueue<Integer>(2);
		assertNull(lcq.poll());
		assertEquals(lcq.size(), 0);
		assertTrue(lcq.offer(1));
		assertTrue(lcq.offer(2));
		assertFalse(lcq.offer(3));
		assertEquals(lcq.poll().intValue(), 1);
		assertEquals(lcq.poll().intValue(), 2);
		assertNull(lcq.poll());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void noNegativeCtor() {
		new LimitedConcurrentQueue<Integer>(-1);
	}
}
