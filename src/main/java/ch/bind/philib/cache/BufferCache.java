package ch.bind.philib.cache;

public interface BufferCache<E> extends ObjectCache<E> {

	void fillZero(E e);
}
