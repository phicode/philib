package ch.bind.philib.pool;

import ch.bind.philib.pool.impl.ObjectPool;

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