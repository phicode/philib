package ch.bind.philib.cache;

import ch.bind.philib.validation.SimpleValidation;

public class SpecificObjectCache<E> implements ObjectCache<E> {

	private final ObjectCache<E> cache;

	protected SpecificObjectCache(ObjectCache<E> cache) {
		SimpleValidation.notNull(cache);
		this.cache = cache;
	}

	@Override
	public final E acquire() {
		return cache.acquire();
	}

	@Override
	public final boolean release(E e) {
		return cache.release(e);
	}
}
