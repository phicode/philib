package ch.bind.philib.pool;

public class TLObjPool<E> implements ObjectPool<E>{

	private final ObjectPoolImpl<E > parent;
	
	private final ThreadLocalBufferPool<E> tl;

	public TLObjPool(int maxEntries) {
		this.parent = new ObjectPoolImpl<E>(maxEntries);
		this.tl= new ThreadLocalBufferPool<E>(parent);
	}
	
	@Override
	public E get(ObjectFactory<E> factory) {
		tl.get().get();
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void release(ObjectFactory<E> factory, E e) {
		// TODO Auto-generated method stub
		
	}
	
	private static final class ThreadLocalBufferPool<E> extends ThreadLocal<ObjectPoolImpl<E >> {
		
		private final ObjectPoolImpl<E> parent;

		ThreadLocalBufferPool(ObjectPoolImpl<E> parent) {
			this.parent = parent;
			
		}
		
		@Override
		protected E initialValue() {
			
		}
		
		@Override
		public void remove() {
			// TODO Auto-generated method stub
			super.remove();
		}
	}
}
