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

	private final String unit;

	private long counts;

	private long total;

	private long min = -1;

	private long max = -1;

	Counter(String name, String unit) {
		this.name = name;
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public String getUnit() {
		return unit;
	}

	/**
	 * @deprecated deprecated in favor of {@link #count(long)}.
	 */
	@Deprecated
	public void add(long value) {
		count(value);
	}

	public void count(long value) {
		if (value <= 0) {
			return;
		}
		synchronized (this) {
			if (counts == 0) {
				counts = 1;
				min = value;
				max = value;
				total = value;
			}
			else {
				counts++;
				total = Calc.unsignedAdd(total, value);
				min = Math.min(min, value);
				max = Math.max(max, value);
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
			return String.format("%s[unit=%s, #counts=0, total=0, min=N/A, max=N/A, avg=N/A]", name, unit);
		}
		double avg = ((double) to) / c;
		return String.format("%s[unit=%s, #counts=%d, total=%d, min=%d, max=%d, avg=%.3f]", name, unit, c, to, mi, ma, avg);
	}
}
