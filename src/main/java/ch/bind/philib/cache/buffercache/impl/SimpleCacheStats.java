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
package ch.bind.philib.cache.buffercache.impl;

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.cache.buffercache.CacheStats;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class SimpleCacheStats implements CacheStats {

	private final AtomicLong acquires = new AtomicLong(0);

	private final AtomicLong creates = new AtomicLong(0);

	private final AtomicLong releases = new AtomicLong(0);

	private final AtomicLong destroyed = new AtomicLong(0);

	void incrementAcquires() {
		acquires.incrementAndGet();
	}

	void incrementCreates() {
		creates.incrementAndGet();
	}

	void incrementReleases() {
		releases.incrementAndGet();
	}

	void incrementDestroyed() {
		destroyed.incrementAndGet();
	}

	@Override
	public long getAcquires() {
		return acquires.get();
	}

	@Override
	public long getCreates() {
		return creates.get();
	}

	@Override
	public long getReleases() {
		return releases.get();
	}

	@Override
	public long getDestroyed() {
		return destroyed.get();
	}

	@Override
	public String toString() {
		return String.format("acquires=%d, creates=%d, releases=%d, destroyed=%d",//
		        acquires.get(), creates.get(), releases.get(), destroyed.get());
	}
}
