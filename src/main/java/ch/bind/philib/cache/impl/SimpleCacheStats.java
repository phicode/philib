package ch.bind.philib.cache.impl;

import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.cache.CacheStats;

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
