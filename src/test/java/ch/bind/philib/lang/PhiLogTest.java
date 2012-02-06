package ch.bind.philib.lang;

import org.junit.Test;

public class PhiLogTest {

	@Test
	public void logSomeStuff() {
		PhiLog pl = new PhiLog(getClass());
		String msg = "message for " + getClass().getSimpleName();
		pl.info("should be info " + msg);
		pl.warn("should be warn " + msg);
	}
}
