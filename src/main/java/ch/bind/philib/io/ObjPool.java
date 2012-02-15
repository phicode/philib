package ch.bind.philib.io;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import ch.bind.philib.validation.SimpleValidation;

public abstract class ObjPool<E> {

	private final AtomicReferenceArray<E> pool;

	private final int maxEntries;

	private final AtomicInteger size = new AtomicInteger(0);

	private final AtomicInteger rpos = new AtomicInteger(0);

	private final AtomicInteger wpos = new AtomicInteger(0);

	public ObjPool(int maxEntries) {
		System.out.println("maxEntries: " + maxEntries);
		this.maxEntries = maxEntries;
		pool = new AtomicReferenceArray<E>(maxEntries);
	}

	protected abstract E create();

	public E get() {
		boolean hasElement = decrementNotNegative(size);
		if (hasElement) {
			int i = getAndIncrementMod(rpos, maxEntries);
			E e = pool.getAndSet(i, null);
			SimpleValidation.notNull(e, "read from pos " + i + ": field is null but should not be");
			return e;
		}
		else {
			return create();
		}
	}

	public void release(final E e) {
		if (e != null) {
			int size = this.size.get();
			if (size < maxEntries) {
				
			}
			boolean canAdd = incrementWithMax(size, maxEntries);
			if (canAdd) {
				int pos = wpos.get();
				if (pool.compareAndSet(pos, null, e)) {
					
				}
				
				int i = getAndIncrementMod(wpos, maxEntries);
				boolean ok = pool.compareAndSet(i, null, e);
				SimpleValidation.isTrue(ok, "write to pos " + i + ": field is not null but should be");
			}
		}
	}

	private boolean incrementWithMax(AtomicInteger ai, int max) {
		while (true) {
			int val = ai.get();
			int newVal = val + 1;
			if (newVal > max) {
				return false;
			}
			else {
				if (ai.compareAndSet(val, newVal)) {
					return true;
				}
			}
		}
	}

	private boolean decrementNotNegative(AtomicInteger ai) {
		while (true) {
			int val = ai.get();
			int newVal = val - 1;
			if (newVal < 0) {
				return false;
			}
			else {
				if (ai.compareAndSet(val, newVal)) {
					return true;
				}
			}
		}
	}

	private static int getAndIncrementMod(AtomicInteger ai, int mod) {
		while (true) {
			int value = ai.get();
			int newVal = (value + 1) % mod;
			if (ai.compareAndSet(value, newVal)) {
				return value;
			}
		}
	}
}
