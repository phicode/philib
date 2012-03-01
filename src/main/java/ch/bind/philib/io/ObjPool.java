package ch.bind.philib.io;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import ch.bind.philib.validation.SimpleValidation;

public abstract class ObjPool<E> {

	private static final int CONC = 8;
	
	private final AtomicBitSet readable;

	private final AtomicBitSet writable;

	private final AtomicReferenceArray<E> pool;

	private final AtomicInteger[] useCount;

	private final int maxEntries;

	// private final AtomicInteger size = new AtomicInteger(0);
	//
	// private final AtomicInteger rpos = new AtomicInteger(0);
	//
	// private final AtomicInteger wpos = new AtomicInteger(0);

	public ObjPool(int maxEntries) {
		this.readable = AtomicBitSet.forNumBits(CONC, false);
		this.writable = AtomicBitSet.forNumBits(CONC, true);
		this.maxEntries = maxEntries;
		this.pool = new AtomicReferenceArray<E>(maxEntries);
		useCount = new AtomicInteger[maxEntries];
		for (int i = 0; i < maxEntries; i++) {
			useCount[i] = new AtomicInteger();
		}
	}

	public void printUseCounts() {
		System.out.println("UseCounts: ");
		for (int i = 0; i < maxEntries; i++) {
			System.out.printf("%10d ", useCount[i].get());
			if (i % 10 == 9) {
				System.out.println();
			}
		}
	}

	protected abstract E create();

	protected abstract void destroy(E e);

	public E get() {
		int idx = readable.switchAnyToFalse();
		if (idx == -1) {
			return create();
		}
		else {
			useCount[idx].incrementAndGet();
			
			E e = pool.getAndSet(idx, null);
			// TODO: make to assert
			SimpleValidation.notNull(e);
			boolean ok = writable.compareFalseAndSwitch(idx);
			SimpleValidation.isTrue(ok);
			return e;
		}
	}

	public void release(final E e) {
		if (e != null) {
			int idx = writable.switchAnyToFalse();
			if (idx == -1) {
				destroy(e);
			}
			else {
				useCount[idx].incrementAndGet();
				
				boolean ok = pool.compareAndSet(idx, null, e);
				// TODO: make to assert
				SimpleValidation.isTrue(ok);
				ok = readable.compareFalseAndSwitch(idx);
				SimpleValidation.isTrue(ok);
			}
		}
	}
}
