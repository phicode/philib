package ch.bind.philib.concurrency;

import java.util.Arrays;
import java.util.Comparator;

public final class LockGroup {

	private static final LockableComparator lockableComparator = new LockableComparator();

	private final Lockable[] objects;

	public LockGroup(Lockable[] objects) {
		if (objects == null || objects.length == 0)
			throw new IllegalArgumentException("TODO"); // TODO
		final int n = objects.length;
		this.objects = new Lockable[n];
		System.arraycopy(objects, 0, this.objects, 0, n);
		// FIXME: the array could contain the same lock twice! remove it? handle
		// it in lock/unlock?
		Arrays.sort(this.objects, lockableComparator);
	}

	public void lock() {
		for (Lockable l : objects) {
			l.lock();
		}
	}

	public void unlock() {
		for (Lockable l : objects) {
			l.unlock();
		}
	}

	private static final class LockableComparator implements
			Comparator<Lockable> {

		@Override
		public int compare(final Lockable o1, final Lockable o2) {
			final long diff = o1.getLockId() - o2.getLockId();
			if (diff > 0)
				return 1;
			if (diff < 0)
				return -1;
			return 0;
		}

	}

}
