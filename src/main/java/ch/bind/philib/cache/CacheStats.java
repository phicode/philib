package ch.bind.philib.cache;

public interface CacheStats {

	long getCreates();

	long getAcquires();

	long getReleases();

	long getDestroyed();

}
