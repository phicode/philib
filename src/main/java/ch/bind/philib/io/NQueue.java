package ch.bind.philib.io;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import ch.bind.philib.validation.SimpleValidation;

/**
 * A notifying queue. Every time an item is added to the queue semaphore is
 * released.
 */
public final class NQueue<E> {

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

	public boolean offer(E e) {
		boolean added = queue.add(e);
		if (added) {
			sem.release();
		}
		return added;
	}

	public int size() {
		return queue.size();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public void clear() {
		queue.clear();
	}

	public E poll() {
		return queue.poll();
	}

	public E peek() {
		return queue.peek();
	}
}
