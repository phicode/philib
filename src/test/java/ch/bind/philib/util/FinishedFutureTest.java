package ch.bind.philib.util;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class FinishedFutureTest {

	@Test
	public void withNull() {
		FinishedFuture<Integer> ff = new FinishedFuture<Integer>(null);
		assertFalse(ff.cancel(true));
		assertFalse(ff.cancel(false));
		assertNull(ff.get());
		assertNull(ff.get(1, TimeUnit.MICROSECONDS));
		assertFalse(ff.isCancelled());
		assertTrue(ff.isDone());
	}

	@Test
	public void withValue() {
		Integer value = 123456;
		FinishedFuture<Integer> ff = new FinishedFuture<Integer>(value);
		assertFalse(ff.cancel(true));
		assertFalse(ff.cancel(false));
		assertEquals(ff.get(), value);
		assertEquals(ff.get(1, TimeUnit.MICROSECONDS), value);
		assertFalse(ff.isCancelled());
		assertTrue(ff.isDone());
	}
}
