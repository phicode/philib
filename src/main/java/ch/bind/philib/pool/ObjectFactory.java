package ch.bind.philib.pool;

public interface ObjectFactory<E> {

	E create();

	void destroy(E e);
}
