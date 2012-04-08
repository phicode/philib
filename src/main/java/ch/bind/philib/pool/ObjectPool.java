package ch.bind.philib.pool;

public interface ObjectPool<E> {

	E get(ObjectFactory<E> factory);

	void release(ObjectFactory<E> factory, E e);
}
