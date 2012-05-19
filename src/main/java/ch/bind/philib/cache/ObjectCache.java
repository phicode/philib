package ch.bind.philib.cache;

public interface ObjectCache<E> {

	E acquire();

	boolean release(E e);
	
}
