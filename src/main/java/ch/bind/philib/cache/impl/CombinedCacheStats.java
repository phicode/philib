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
