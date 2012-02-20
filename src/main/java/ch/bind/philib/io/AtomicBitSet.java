package ch.bind.philib.io;

import java.util.concurrent.atomic.AtomicLong;

public final class AtomicBitSet {

	private final AtomicLong[] bs;

	private final int numBits;

	public AtomicBitSet(int numBits, boolean initialValue) {
		this.numBits = numBits;
		int numEntries = numBits / 64;
		if (numEntries * 64 < numBits) {
			numEntries++;
		}
		bs = new AtomicLong[numEntries];
		for (int i = 0; i < numEntries; i++) {
			bs[i] = new AtomicLong();
		}
		setAll(initialValue);
	}

	private void setAll(boolean value) {
		long v = value ? 0xFFFFFFFFFFFFFFFFL : 0;
		for (AtomicLong b : bs) {
			b.set(v);
		}
	}
	
	
	public int switchAnyToFalse() {
		int startBucket = (int)(Thread.currentThread().getId() % bs.length);
		for (int i = 0; i < bs.length; i++) {
			int bucketIdx = (startBucket + i) % bs.length;
			AtomicLong b = bs[bucketIdx];
			long v = b.get();
			if (v != 0) {
				
			}
		}
		
	}

	// public boolean get(int idx) {
	// rangeCheck(idx);
	// int chunk = idx / 64;
	// int bit = idx & 63; // mod 64
	// long mask = 1 << bit;
	// AtomicLong b = bs[chunk];
	// long v = b.get();
	// return ((v & mask) == mask);
	// }

	public boolean compareTrueAndSwitch(int idx) {
		rangeCheck(idx);
		int chunk = idx / 64;
		int bit = idx & 63; // mod 64
		long mask = 1 << bit;
		AtomicLong b = bs[chunk];
		while (true) {
			long v = b.get();
			boolean set = ((v & mask) == mask);
			if (set) {
				long newV = v & (~mask);
				if (b.compareAndSet(v, newV)) {
					return true;
				}
			}
			else {
				// bit is not set, cant switch
				return false;
			}
		}
	}

	public boolean compareFalseAndSwitch(int idx) {
		rangeCheck(idx);
		int chunk = idx / 64;
		int bit = idx & 63; // mod 64
		long mask = 1 << bit;
		AtomicLong b = bs[chunk];
		while (true) {
			long v = b.get();
			boolean set = ((v & mask) == mask);
			if (set) {
				// bit is set, cant switch
				return false;
			}
			else {
				long newV = v | mask;
				if (b.compareAndSet(v, newV)) {
					return true;
				}
			}
		}
	}

	private void rangeCheck(int idx) {
		if (idx < 0 || idx >= numBits) {
			throw new IllegalArgumentException("index is out of bounds");
		}
	}
}
