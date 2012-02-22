package ch.bind.philib.io;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import ch.bind.philib.validation.SimpleValidation;

public abstract class ObjPool<E> {

	private final AtomicBitSet readable;

	private final AtomicBitSet writable;

	private final AtomicReferenceArray<E> pool;

	private final int maxEntries;

	// private final AtomicInteger size = new AtomicInteger(0);
	//
	// private final AtomicInteger rpos = new AtomicInteger(0);
	//
	// private final AtomicInteger wpos = new AtomicInteger(0);

	public ObjPool(int maxEntries) {
		this.readable = new AtomicBitSet(maxEntries, false);
		this.writable = new AtomicBitSet(maxEntries, true);
		this.maxEntries = maxEntries;
		this.pool = new AtomicReferenceArray<E>(maxEntries);
	}

	protected abstract E create();

	protected abstract void destroy(E e);

	public E get() {
		int idx = readable.switchAnyToFalse();
		if (idx == -1) {
			return create();
		}
		else {
			E e = pool.getAndSet(idx, null);
			// TODO: make to assert
			SimpleValidation.notNull(e);
			boolean ok = writable.compareFalseAndSwitch(idx);
			SimpleValidation.isTrue(ok);
			return e;
		}
		//
		// boolean hasElement = decrementNotNegative(size);
		// if (hasElement) {
		//
		// int i = getAndIncrementMod(rpos, maxEntries);
		// E e = pool.getAndSet(i, null);
		// SimpleValidation.notNull(e, "read from pos " + i +
		// ": field is null but should not be");
		// return e;
		// }
		// else {
		// return create();
		// }
	}

	public void release(final E e) {
		if (e != null) {
			int idx = writable.switchAnyToFalse();
			if (idx == -1) {
				destroy(e);
			}
			else {
				boolean ok = pool.compareAndSet(idx, null, e);
				// TODO: make to assert
				SimpleValidation.isTrue(ok);
				ok = readable.compareFalseAndSwitch(idx);
				SimpleValidation.isTrue(ok);
			}
		}

		// if (e != null) {
		// while (true) {
		// int size = this.size.get();
		// if (size < maxEntries) {
		// int p = getAndIncrementMod(wpos, maxEntries);
		// // int pos = wpos.get();
		// // int nextPos = (pos + 1) % maxEntries;
		// if (pool.compareAndSet(p, null, e)) {
		// // incMod(wpos, maxEntries);
		// // boolean ok = wpos.compareAndSet(pos, nextPos);
		// // SimpleValidation.isTrue(ok);
		// // this.size.incrementAndGet();
		// // ok = this.size.compareAndSet(size, size +1);
		// // SimpleValidation.isTrue(ok);
		// this.size.incrementAndGet();
		// return;
		// }
		// else {
		// throw new Error();
		// }
		// }
		// else {
		// return;
		// }
		// }
		// boolean canAdd = incrementWithMax(size, maxEntries);
		// if (canAdd) {
		//
		//
		// int i = getAndIncrementMod(wpos, maxEntries);
		// boolean ok = pool.compareAndSet(i, null, e);
		// SimpleValidation.isTrue(ok, "write to pos " + i +
		// ": field is not null but should be");
		// }
		// }
	}

	// private boolean incrementWithMax(AtomicInteger ai, int max) {
	// while (true) {
	// int val = ai.get();
	// int newVal = val + 1;
	// if (newVal > max) {
	// return false;
	// }
	// else {
	// if (ai.compareAndSet(val, newVal)) {
	// return true;
	// }
	// }
	// }
	// }
	//
	// private boolean decrementNotNegative(AtomicInteger ai) {
	// while (true) {
	// int val = ai.get();
	// int newVal = val - 1;
	// if (newVal < 0) {
	// return false;
	// }
	// else {
	// if (ai.compareAndSet(val, newVal)) {
	// return true;
	// }
	// }
	// }
	// }
	//
	// private static int getAndIncrementMod(AtomicInteger ai, int mod) {
	// while (true) {
	// int value = ai.get();
	// int newVal = (value + 1) % mod;
	// if (ai.compareAndSet(value, newVal)) {
	// return value;
	// }
	// }
	// }
	//
	// private static void incMod(AtomicInteger ai, int mod) {
	// while (true) {
	// int value = ai.get();
	// int newVal = (value + 1) % mod;
	// if (ai.compareAndSet(value, newVal)) {
	// return;
	// }
	// }
	// }
}
