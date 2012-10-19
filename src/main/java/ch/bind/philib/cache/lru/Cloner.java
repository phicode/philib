package ch.bind.philib.cache.lru;

public interface Cloner<V> {

	V cloneValue(V value);

}
