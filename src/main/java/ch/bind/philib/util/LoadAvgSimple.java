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

public final class LoadAvgSimple implements LoadAvg {

	private final long lAvgOfXNs;

	private final double fAvgOfXNs;

	private long tWork;

	private long tIdle;

	private long lastNormalizeNs;

	private LoadAvgSimple(long avgOfXNs) {
		this.lAvgOfXNs = avgOfXNs;
		this.fAvgOfXNs = avgOfXNs;
		// no work has been logged yet
		this.tIdle = avgOfXNs;
	}

	public static LoadAvgSimple forSeconds(int secs) {
		Validation.isTrue(secs >= 1);
		return new LoadAvgSimple(secs * 1000000000L);
	}

	public static LoadAvgSimple forMillis(int millis) {
		Validation.isTrue(millis >= 1);
		return new LoadAvgSimple(millis * 1000000L);
	}

	public static LoadAvgSimple forMicros(int micros) {
		Validation.isTrue(micros >= 1);
		return new LoadAvgSimple(micros * 1000L);
	}

	public static LoadAvgSimple forNanos(int nanos) {
		Validation.isTrue(nanos >= 1);
		return new LoadAvgSimple(nanos);
	}

	@Override
	public void logWorkMs(long workMs) {
		logWorkNs(workMs * 1000000L);
	}

	@Override
	public void logWorkNs(long workNs) {
		long now = System.nanoTime();
		long diff;
		if (lastNormalizeNs == 0) {
			diff = lAvgOfXNs;
		} else {
			diff = now - lastNormalizeNs;
			if (diff < 0) {
				diff = 0;
			}
		}
		lastNormalizeNs = now;
		tWork += workNs;
		tIdle += Math.max(0, diff - workNs);
		long total = tIdle + tWork;
		double factor = fAvgOfXNs / total;
		tIdle = (long) (tIdle * factor);
		tWork = (long) (tWork * factor);
	}

	@Override
	public long getLoadAvg() {
		// update the internal state first
		logWorkNs(0);
		return tWork;
	}

	@Override
	public double getLoadAvgAsFactor() {
		return getLoadAvg() / fAvgOfXNs;
	}
}
