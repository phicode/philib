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
package ch.bind.philib.cache.impl;

import ch.bind.philib.cache.CacheStats;

public final class CombinedCacheStats implements CacheStats {

	private CacheStats[] stats;

	CombinedCacheStats(CacheStats[] stats) {
		this.stats = stats;
	}

	@Override
	public long getAcquires() {
		long a = 0;
		for (CacheStats s : stats) {
			a += s.getAcquires();
		}
		return a;
	}

	@Override
	public long getCreates() {
		long c = 0;
		for (CacheStats s : stats) {
			c += s.getCreates();
		}
		return c;
	}

	@Override
	public long getReleases() {
		long r = 0;
		for (CacheStats s : stats) {
			r += s.getReleases();
		}
		return r;
	}

	@Override
	public long getDestroyed() {
		long d = 0;
		for (CacheStats s : stats) {
			d += s.getDestroyed();
		}
		return d;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("cache-total: acquires=");
		sb.append(getAcquires());
		sb.append(", creates=");
		sb.append(getCreates());
		sb.append(", releases=");
		sb.append(getReleases());
		sb.append(", destroyed=");
		sb.append(getDestroyed());
		for (int i = 0; i < stats.length; i++) {
			sb.append("\n");
			sb.append("  bucket-");
			sb.append(i);
			sb.append(": ");
			sb.append(stats[i]);
		}
		return sb.toString();
	}
}
