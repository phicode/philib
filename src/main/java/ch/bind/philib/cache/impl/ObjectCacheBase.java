package ch.bind.philib.cache.impl;

import ch.bind.philib.cache.ObjectCache;
import ch.bind.philib.validation.SimpleValidation;

public abstract class ObjectCacheBase<E> implements ObjectCache<E> {

	private final ObjectFactory<E> factory;

	public ObjectCacheBase(ObjectFactory<E> factory) {
		SimpleValidation.notNull(factory);
		this.factory = factory;
	}

	@Override
	public final E acquire() {
		do {
			E e = tryAcquire();
			if (e == null) {
				return factory.create();
			} else {
				if (factory.canReuse(e)) {
					return e;
				} else {
					factory.destroy(e);
				}
			}
		} while (true);
	}

	@Override
	public final boolean release(final E e) {
		if (e != null && factory.release(e)) {// && factory.canRelease(e)) {
			if (tryRelease(e)) {
				return true;
			} else {
				factory.destroy(e);
			}
		}
		return false;
	}

	protected abstract E tryAcquire();

	protected abstract boolean tryRelease(E e);
}
