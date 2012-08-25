package ch.bind.philib.util;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class SimpleLoadAvgTest {
	@Test
	public void withMillis() {
		LoadAvg la = SimpleLoadAvg.forMillis(500);
		verify(la, 0, 0.01);

		// almost 0 load
		simulateLoadMs(la, 0, 100, 1000);
		verify(la, 0, 0.02);

		// approx 25% load
		simulateLoadMs(la, 25, 75, 1000);
		verify(la, 0.2, 0.3);

		//approx 50% load
		simulateLoadMs(la, 50, 50, 1000);
		verify(la, 0.4, 0.6);

		//approx 75% load
		simulateLoadMs(la, 75, 25, 1000);
		verify(la, 0.6, 0.8);

		// full load
		simulateLoadMs(la, 100, 0, 1000);
		verify(la, 0.8, 1);

		simulateLoadMs(la, 75, 25, 1000);
		verify(la, 0.6, 0.8);

		simulateLoadMs(la, 50, 50, 1000);
		verify(la, 0.4, 0.6);

		simulateLoadMs(la, 25, 75, 1000);
		verify(la, 0.2, 0.4);

		simulateLoadMs(la, 0, 100, 1000);
		verify(la, 0, 0.1);
	}

	private void verify(LoadAvg la, double min, double max) {
		double factor = la.getLoadAvgAsFactor();
		assertTrue(factor >= min, factor + " should be >= " + min);
		assertTrue(factor <= max, factor + " should be <= " + max);
	}

	private void simulateLoadMs(LoadAvg la, int work, int idle, int durationMs) {
		long durationNs = durationMs * 1000_000L;
		long workNs = work * 1000_000L;
		long idleNs = idle * 1000_000L;
		long now = System.nanoTime();
		long end = now + durationNs;
		boolean atWork = true;
		long switchWorkIdleAt = now + workNs;

		la.start();
		while (end > now) {
			now = System.nanoTime();
			if (now > switchWorkIdleAt) {
				if (atWork) {
					la.end();
					switchWorkIdleAt = now + idleNs;
				} else {
					la.start();
					switchWorkIdleAt = now + workNs;
				}
				atWork = !atWork;
			}
		}
		la.end();
	}
}
