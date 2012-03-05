/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.bind.philib.io;

import java.util.concurrent.atomic.AtomicLong;

public final class AtomicBitSet {

	private final AtomicLong[] bs;

	private final int numBits;

	private AtomicBitSet(int numBuckets, boolean initialValue) {
		bs = new AtomicLong[numBuckets];
		for (int i = 0; i < numBuckets; i++) {
			bs[i] = new AtomicLong();
		}
		setAll(initialValue);
		numBits = numBuckets * 64;
	}

	public static AtomicBitSet forNumBuckets(int numBuckets, boolean initialValue) {
		return new AtomicBitSet(numBuckets, initialValue);
	}

	public static AtomicBitSet forNumBits(int numBits, boolean initialValue) {
		int numBuckets = numBits / 64;
		if (numBuckets * 64 < numBits) {
			numBuckets++;
		}
		return new AtomicBitSet(numBuckets, initialValue);
	}

	private void setAll(boolean value) {
		long v = value ? 0xFFFFFFFFFFFFFFFFL : 0;
		for (AtomicLong b : bs) {
			b.set(v);
		}
	}

	public int switchAnyToFalse() {
		final int startBucket = (int) (Thread.currentThread().getId() % bs.length);
		// final int startBucket = 0;
		for (int i = 0; i < bs.length; i++) {
			final int bucketIdx = (startBucket + i) % bs.length;
			int idx = switchAnyToFalseInBucket(bucketIdx);
			if (idx != -1) {
				return idx;
			}
		}
		return -1;
	}

	private int switchAnyToFalseInBucket(int bucketIdx) {
		final AtomicLong b = bs[bucketIdx];
		long v = b.get();
		while (v != 0) {
			final int bitIdx = BitOps.findLowestSetBitIdx64(v);
			assert (bitIdx != -1); // v!=0, there must be a bit set
			final long mask = 1L << bitIdx;
			final long newV = v ^ mask;
			if (b.compareAndSet(v, newV)) {
				return (bucketIdx * 64) + bitIdx;
			} else {
				v = b.get();
			}
		}
		return -1;
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
		long mask = 1L << bit;
		AtomicLong b = bs[chunk];
		while (true) {
			long v = b.get();
			boolean set = ((v & mask) == mask);
			if (set) {
				long newV = v ^ mask;
				if (b.compareAndSet(v, newV)) {
					return true;
				}
			} else {
				// bit is not set, cant switch
				return false;
			}
		}
	}

	public boolean compareFalseAndSwitch(int idx) {
		rangeCheck(idx);
		int chunk = idx / 64;
		int bit = idx & 63; // mod 64
		long mask = 1L << bit;
		AtomicLong b = bs[chunk];
		while (true) {
			long v = b.get();
			boolean set = ((v & mask) == mask);
			if (set) {
				// bit is set, cant switch
				return false;
			} else {
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
