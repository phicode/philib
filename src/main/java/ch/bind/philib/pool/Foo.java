package ch.bind.philib.pool;

public interface Foo<E> {

	E get();

	void release(E e);
	
}
