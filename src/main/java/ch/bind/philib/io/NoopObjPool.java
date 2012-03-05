package ch.bind.philib.io;

public abstract class NoopObjPool<E> {

	public NoopObjPool(int maxBuffers) {
		// TODO Auto-generated constructor stub
	}

	protected abstract void destroy(E e);

	protected abstract E create();

	public void release(E e) {
	}

	public E get() {
		return create();
	}
}