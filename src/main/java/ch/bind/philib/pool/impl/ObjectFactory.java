package ch.bind.philib.pool.impl;

public interface ObjectFactory<E> {

	E create();

	void destroy(E e);
	
	void released(E e);
	
	boolean canRelease(E e);
}
