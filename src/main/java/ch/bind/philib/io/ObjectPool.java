package ch.bind.philib.io;

import java.util.concurrent.atomic.AtomicReference;

public abstract class ObjectPool<E> {

	private final AtomicReference<Node<E>> freeList = new AtomicReference<Node<E>>();

	private final AtomicReference<Node<E>> objList = new AtomicReference<Node<E>>();

	public ObjectPool(int maxEntries) {
		super();
		for (int i = 0; i < maxEntries; i++) {
			Node<E> n = new Node<E>();
			put(freeList, n);
		}
	}

	protected abstract E create();

	public E get() {
		Node<E> node = take(objList);
		if (node == null) {
			return create();
		}
		else {
			E e = node.entry.getAndSet(null);
			put(freeList, node);
			return e;
		}
	}

	public void release(E e) {
		if (e != null) {
			Node<E> node = take(freeList);
			if (node != null) {
				node.entry.set(e);
				put(objList, node);
			}
		}
	}

	private void put(AtomicReference<Node<E>> rootRef, Node<E> node) {
		boolean done = false;
		do {
			Node<E> oldHead = rootRef.get();
			if (oldHead == null) {
				node.tail.set(null);
				done = rootRef.compareAndSet(null, node);
			}
			else {
				node.tail.set(oldHead);
				done = rootRef.compareAndSet(oldHead, node);
			}
		} while (!done);
	}

	private Node<E> take(AtomicReference<Node<E>> rootRef) {
		boolean done = false;
		Node<E> head;
		do {
			head = rootRef.get();
			if (head == null) { // empty
				return null;
			}
			Node<E> newHead = head.tail.get();
			done = rootRef.compareAndSet(head, newHead);
		} while (!done);
		head.tail.set(null);
		return head;
	}

	private static final class Node<E> {
		private final AtomicReference<Node<E>> tail = new AtomicReference<Node<E>>();

		private final AtomicReference<E> entry = new AtomicReference<E>();
	}

}
