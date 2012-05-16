package ch.bind.philib.pool.impl;


public interface ObjectPool<E> {

	E get(ObjectFactory<E> factory);

	void release(ObjectFactory<E> factory, E e);
}
