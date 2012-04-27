package ch.bind.philib.pool;

import java.util.concurrent.atomic.AtomicInteger;

public class ScalableObjectPool<E> implements ObjectPool<E> {

    private BoundToThreadLocal binder = new BoundToThreadLocal();

    private ObjectPoolImpl<E>[] many;
    private AtomicInteger[] bindCount;

    public ScalableObjectPool(int maxEntries) {
        this(maxEntries, Runtime.getRuntime().availableProcessors());
    }

    public ScalableObjectPool(int maxEntries, int numBuckets) {
        // System.out.println("maxEntries=" + maxEntries + ",numBuckets=" +
        // numBuckets);
        int entriesPerBucket = maxEntries / numBuckets;
        if (maxEntries % numBuckets != 0) {
            entriesPerBucket++;
        }
        many = new ObjectPoolImpl[numBuckets];
        bindCount = new AtomicInteger[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            many[i] = new ObjectPoolImpl<E>(entriesPerBucket);
            bindCount[i] = new AtomicInteger(0);
        }
    }

    @Override
    public E get(ObjectFactory<E> factory) {
        return binder.get().get(factory);
    }

    @Override
    public void release(ObjectFactory<E> factory, E e) {
        binder.get().release(factory, e);
    }

    private ObjectPoolImpl<E> bind() {
        int idx = indexOfLowestBound();
        bindCount[idx].incrementAndGet();
        return many[idx];
    }

    private void unbind(ObjectPoolImpl<E> p) {
        final int N = many.length;
        for (int i = 0; i < N; i++) {
            if (many[i] == p) {
                bindCount[i].decrementAndGet();
            }
        }
    }

    // this can be made generic
    private int indexOfLowestBound() {
        final int N = bindCount.length;
        int idx = 0;
        int lowest = bindCount[0].get();
        for (int i = 1; i < N; i++) {
            int cur = bindCount[i].get();
            if (cur < lowest) {
                lowest = cur;
                idx = i;
            }
        }
        return idx;
    }

    private final class BoundToThreadLocal extends ThreadLocal<ObjectPoolImpl<E>> {

        @Override
        protected ObjectPoolImpl<E> initialValue() {
            return bind();
        }
    }
}
