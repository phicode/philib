package ch.bind.philib.cache.impl;

public final class NoopObjectCache<E> extends ObjectCacheBase<E> {

	public NoopObjectCache(ObjectFactory<E> factory) {
		super(factory);
	}

	@Override
	protected E tryAcquire() {
		return null;
	}

	@Override
	protected boolean tryRelease(E e) {
		return false;
	}
}