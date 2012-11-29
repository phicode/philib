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
package ch.bind.philib.pool.object;

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.pool.PoolStats;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class SimplePoolStats implements PoolStats {

	private final AtomicLong creates = new AtomicLong(0);

	private final AtomicLong takes = new AtomicLong(0);

	private final AtomicLong recycled = new AtomicLong(0);

	private final AtomicLong released = new AtomicLong(0);

	void incrementCreates() {
		creates.incrementAndGet();
	}

	void incrementTakes() {
		takes.incrementAndGet();
	}

	void incrementRecycled() {
		recycled.incrementAndGet();
	}

	void incrementReleased() {
		released.incrementAndGet();
	}

	@Override
	public long getCreates() {
		return creates.get();
	}

	@Override
	public long getTakes() {
		return takes.get();
	}

	@Override
	public long getRecycled() {
		return recycled.get();
	}

	@Override
	public long getReleased() {
		return released.get();
	}

	@Override
	public String toString() {
		return String.format("creates=%d, takes=%d, recycled=%d, released=%d",//
				creates.get(), takes.get(), recycled.get(), released.get());
	}
}