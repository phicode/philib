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

import ch.bind.philib.validation.Validation;

public final class SimpleLoadAvg implements LoadAvg {

	private final long lAvgOfXNs;

	private final double fAvgOfXNs;

	private long tWork;

	private long tIdle;

	private long startAt;

	private long endAt;

	private SimpleLoadAvg(long avgOfXNs) {
		this.lAvgOfXNs = avgOfXNs;
		this.fAvgOfXNs = avgOfXNs;
		// no work has been logged yet
		this.tIdle = avgOfXNs;
	}

	public static SimpleLoadAvg forSeconds(int secs) {
		Validation.isTrue(secs >= 1);
		return new SimpleLoadAvg(secs * 1_000_000_000L);
	}

	public static SimpleLoadAvg forMillis(int millis) {
		Validation.isTrue(millis >= 1);
		return new SimpleLoadAvg(millis * 1_000_000L);
	}

	public static SimpleLoadAvg forMicros(int micros) {
		Validation.isTrue(micros >= 1);
		return new SimpleLoadAvg(micros * 1000L);
	}

	public static SimpleLoadAvg forNanos(int nanos) {
		Validation.isTrue(nanos >= 1);
		return new SimpleLoadAvg(nanos);
	}

	@Override
	public void start() {
		if (startAt == 0) {
			startAt = System.nanoTime();
			if (endAt > 0) {
				long diff = startAt - endAt;
				if (diff > 0) {
					tIdle += diff;
					normalize();
				}
			}
		}
	}

	@Override
	public void end() {
		if (startAt != 0) {
			long s = startAt;
			startAt = 0;
			endAt = System.nanoTime();
			long diff = endAt - s;
			if (diff > 0) {
				tWork += diff;
				normalize();
			}
		}
	}

	private void normalize() {
		long total = tIdle + tWork;
		if (total > lAvgOfXNs) {
			double factor = fAvgOfXNs / total;
			tIdle = (long) (tIdle * factor);
			tWork = (long) (tWork * factor);
		}
	}

	@Override
	public long getLoadAvg() {
		return tWork;
	}

	@Override
	public double getLoadAvgAsFactor() {
		return tWork / fAvgOfXNs;
	}
}
