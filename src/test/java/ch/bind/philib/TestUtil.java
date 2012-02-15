package ch.bind.philib;

public class TestUtil {

	private static final long DEFAULT_SLEEPTIME_MS = 500;

	private TestUtil() {
	}

	public static void gcAndSleep() {
		gcAndSleep(DEFAULT_SLEEPTIME_MS);
	}

	public static void gcAndSleep(long sleepTime) {
		System.gc();
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			throw new RuntimeException("interrupted while sleeping for a test!");
		}
	}

}
