package ch.bind.philib.io;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import ch.bind.philib.validation.SimpleValidation;

/**
 * A notifying queue. Every time an item is added to the queue semaphore is
 * released.
 */
public final class NQueue<E> implements Queue<E> {

	private final Semaphore sem;
	private final Queue<E> queue;

	public NQueue(Semaphore sem) {
		this(sem, new ConcurrentLinkedQueue<E>());
	}

	public NQueue(Semaphore sem, Queue<E> queue) {
		SimpleValidation.notNull(sem);
		SimpleValidation.notNull(queue);
		this.sem = sem;
		this.queue = queue;
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return queue.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return queue.iterator();
	}

	@Override
	public Object[] toArray() {
		return queue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return queue.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return queue.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return queue.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return queue.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return queue.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return queue.retainAll(c);
	}

	@Override
	public void clear() {
		queue.clear();
	}

	@Override
	public boolean add(E e) {
		boolean added = queue.add(e);
		if (added) {
			sem.release();
		}
		return added;
	}

	@Override
	public boolean offer(E e) {
		boolean added = queue.offer(e);
		if (added) {
			sem.release();
		}
		return added;
	}

	@Override
	public E remove() {
		return queue.remove();
	}

	@Override
	public E poll() {
		return queue.poll();
	}

	@Override
	public E element() {
		return queue.element();
	}

	@Override
	public E peek() {
		return queue.peek();
	}
}
