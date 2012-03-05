package ch.bind.philib.io;

public interface ObjPoolType<E> {

	E create();

	void destroy(E e);
}
