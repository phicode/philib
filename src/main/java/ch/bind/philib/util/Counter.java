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

/** A simple counter where values can be added or the whole counter be reset. */
public final class Counter {

	private final String name;

	private long counts;

	private long total;

	private long min = -1;

	private long max = -1;

	public Counter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public synchronized long getNumCounts() {
		return counts;
	}

	public synchronized long getTotal() {
		return total;
	}

	public synchronized long getMin() {
		return min;
	}

	public synchronized long getMax() {
		return max;
	}

	public void count(long value) {
		if (value < 0) {
			return;
		}
		synchronized (this) {
			long c = counts++;
			if (c == 0) {
				min = value;
				max = value;
				total = value;
			}
			else {
				min = Math.min(min, value);
				max = Math.max(max, value);
				total = Calc.unsignedAdd(total, value);
			}
		}
	}

	public synchronized void reset() {
		counts = 0;
		total = 0;
		min = -1;
		max = -1;
	}

	@Override
	public String toString() {
		long c, mi, ma, to;
		synchronized (this) {
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

	public void count(Counter counter) {
		long c, mi, ma, to;
		synchronized (counter) {
			c = counter.counts;
			mi = counter.min;
			ma = counter.max;
			to = counter.total;
		}
		if (c == 0) {
			return;
		}
		synchronized (this) {
			if (counts == 0) {
				min = mi;
				max = ma;
			}
			else {
				min = Math.min(min, mi);
				max = Math.max(max, ma);
			}
			counts += c;
			total = Calc.unsignedAdd(total, to);
		}
	}
}
