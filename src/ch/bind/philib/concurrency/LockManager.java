package ch.bind.philib.concurrency;

import java.util.concurrent.atomic.AtomicLong;

public class LockManager {

	private static final AtomicLong LOCK_ID_SEQUENCES = new AtomicLong(0);

	public static long getNextLockId() {
		return LOCK_ID_SEQUENCES.getAndIncrement();
	}
}
