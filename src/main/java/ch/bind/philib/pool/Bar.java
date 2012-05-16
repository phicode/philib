package ch.bind.philib.pool;

import ch.bind.philib.pool.impl.ObjectFactory;
import ch.bind.philib.pool.impl.ObjectPool;
import ch.bind.philib.validation.SimpleValidation;

public class Bar<E> implements Foo<E> {

	private final ObjectFactory<E> factory;

	private final ObjectPool<E> pool;

	protected Bar(ObjectFactory<E> factory, ObjectPool<E> pool) {
		SimpleValidation.notNull(factory);
		SimpleValidation.notNull(pool);
		this.factory = factory;
		this.pool = pool;
	}

	@Override
	public E get() {
		return pool.get(factory);
	}

	@Override
	public void release(E e) {
		pool.release(factory, e);
	}
}
