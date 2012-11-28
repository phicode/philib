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

import ch.bind.philib.cache.buffercache.BufferCacheStats;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class SimpleBufferCacheStats implements BufferCacheStats {

	private final AtomicLong acquires = new AtomicLong(0);

	private final AtomicLong creates = new AtomicLong(0);

	private final AtomicLong freed = new AtomicLong(0);

	private final AtomicLong discarded = new AtomicLong(0);

	void incrementAcquires() {
		acquires.incrementAndGet();
	}

	void incrementCreates() {
		creates.incrementAndGet();
	}

	void incrementFreed() {
		freed.incrementAndGet();
	}

	void incrementDiscarded() {
		discarded.incrementAndGet();
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
	public long getFreed() {
		return freed.get();
	}

	@Override
	public long getDiscarded() {
		return discarded.get();
	}

	@Override
	public String toString() {
		return String.format("acquires=%d, creates=%d, freed=%d, discarded=%d",//
				acquires.get(), creates.get(), freed.get(), discarded.get());
	}
}
