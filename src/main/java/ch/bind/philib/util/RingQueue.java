package ch.bind.philib.util;

import java.util.concurrent.atomic.AtomicInteger;

import ch.bind.philib.validation.Validation;

public class RingQueue<E> {

	private AtomicInteger writeIdx = new AtomicInteger();
	private AtomicInteger writeCommittedIdx = new AtomicInteger(-1);
	private AtomicInteger readIdx = new AtomicInteger(-1);
	private E[] entries;

	public RingQueue(int numEntries) {
		Validation.isTrue(numEntries > 0, "numEntries must be > 0");
		this.entries = (E[]) new Object[numEntries];
	}

	public boolean offer(E e) {
		Validation.notNull(e);
		int wIdx = writeIdx.get();
		return false;
	}
}
