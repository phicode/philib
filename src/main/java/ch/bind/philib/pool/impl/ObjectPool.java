package ch.bind.philib.pool.impl;

import ch.bind.philib.pool.ObjectFactory;

public interface ObjectPool<E> {

	E get(ObjectFactory<E> factory);

	void release(ObjectFactory<E> factory, E e);
}
