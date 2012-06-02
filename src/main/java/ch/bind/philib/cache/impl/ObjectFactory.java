package ch.bind.philib.cache.impl;

public interface ObjectFactory<E> {

	E create();

	void destroy(E e);

	boolean release(E e);

	boolean canReuse(E e);
}
