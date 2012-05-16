package ch.bind.philib.pool.impl;


public class NoopObjPool<E> implements ObjectPool<E> {

	@Override
	public E get(ObjectFactory<E> factory) {
		return factory.create();
	}

	@Override
	public void release(ObjectFactory<E> factory, E e) {
		factory.destroy(e);
	}
}