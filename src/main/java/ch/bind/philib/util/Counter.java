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

package ch.bind.philib.util;

import ch.bind.philib.math.Calc;

/**
 * A simple counter where values can be added or the whole counter be reset.
 */
public final class Counter {

	// limit the number of counter buckets that are created
	// the performance does go up when the number of counter buckets is increased
	// on the other hand we probably want to keep the number of "support/monitoring-objects" within reason
	// plus: on a _large_ rig the number of buckets may very well lead to increased memory pressure
	private static final int MAX_NUM_COUNTER_BUCKETS = 8;

	private final Object lock = new Object();
	private final String name;
	private final CounterBucket[] buckets;
	private long counts;
	private long total;
	private long min = -1;
	private long max = -1;

	public Counter(String name) {
		this.name = name;
		int nBuckets = Math.min(MAX_NUM_COUNTER_BUCKETS, Runtime.getRuntime().availableProcessors());
		this.buckets = new CounterBucket[nBuckets];
		for (int i = 0; i < nBuckets; i++) {
			buckets[i] = new CounterBucket();
		}
	}

	public String getName() {
		return name;
	}

	public long getNumCounts() {
		synchronized (lock) {
			aggregate();
			return counts;
		}
	}

	public long getTotal() {
		synchronized (lock) {
			aggregate();
			return total;
		}
	}

	public long getMin() {
		synchronized (lock) {
			aggregate();
			return min;
		}
	}

	public long getMax() {
		synchronized (lock) {
			aggregate();
			return max;
		}
	}

	private void aggregate() {
		for (CounterBucket cb : buckets) {
			synchronized (cb) {
				if (cb.counts == 0) {
					continue;
				}
				if (this.counts == 0) {
					this.min = cb.min;
					this.max = cb.max;
				} else {
					this.min = Math.min(this.min, cb.min);
					this.max = Math.max(this.max, cb.max);
				}
				this.counts += cb.counts;
				this.total += cb.total;
				cb.reset();
			}
		}
	}

	public void count(long value) {
		if (value < 0) {
			return;
		}
		int bucketIdx = (int) (Thread.currentThread().getId() % buckets.length);
		CounterBucket cb = buckets[bucketIdx];
		synchronized (cb) {
			cb.count(value);
		}
	}

	public void count(Counter counter) {
		if (counter == null) {
			return;
		}
		long c, mi, ma, to;
		synchronized (counter.lock) {
			counter.aggregate();
			c = counter.counts;
			mi = counter.min;
			ma = counter.max;
			to = counter.total;
		}
		if (c == 0) {
			return;
		}
		synchronized (lock) {
			aggregate();
			if (counts == 0) {
				min = mi;
				max = ma;
			} else {
				min = Math.min(min, mi);
				max = Math.max(max, ma);
			}
			counts += c;
			total = Calc.unsignedAdd(total, to);
		}
	}

	public void reset() {
		synchronized (lock) {
			// resets all the buckets
			aggregate();

			counts = 0;
			total = 0;
			min = -1;
			max = -1;
		}
	}

	@Override
	public String toString() {
		long c, mi, ma, to;
		synchronized (lock) {
			aggregate();
			c = counts;
			mi = min;
			ma = max;
			to = total;
		}

		if (c == 0) {
			return String.format("%s[counts=0, total=0, min=N/A, max=N/A, avg=N/A]", name);
		}
		double avg = ((double) to) / c;
		return String.format("%s[counts=%d, total=%d, min=%d, max=%d, avg=%.3f]", name, c, to, mi, ma, avg);
	}

	private static final class CounterBucket {

		private long counts;
		private long total;
		private long min = -1;
		private long max = -1;

		@SuppressWarnings("unused")
		private volatile long cacheLinePadding1;
		@SuppressWarnings("unused")
		private volatile long cacheLinePadding2;
		@SuppressWarnings("unused")
		private volatile long cacheLinePadding3;
		@SuppressWarnings("unused")
		private volatile long cacheLinePadding4;

		void count(long value) {
			long c = counts++;
			if (c == 0) {
				min = value;
				max = value;
				total = value;
			} else {
				min = Math.min(min, value);
				max = Math.max(max, value);
				total = Calc.unsignedAdd(total, value);
			}
		}

		void reset() {
			this.counts = 0;
			this.total = 0;
			this.min = 0;
			this.max = 0;
		}
	}
}
