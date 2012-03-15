package ch.bind.philib.pool;

public interface ObjectPool<E> {

	E get(final ObjectFactory<E> factory);

	void release(final ObjectFactory<E> factory, final E e);
}
