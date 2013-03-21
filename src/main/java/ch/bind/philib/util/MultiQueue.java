package ch.bind.philib.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class MultiQueue<E> {

	private final Lock mu = new ReentrantLock();

	private Elem<E> head;

	private Elem<E> tail;

	private boolean closed;

	public MultiQueue() {
		Elem<E> e = new Elem<E>(this);
		head = e;
		tail = e;
	}

	public void publish(E value) {
		if (value == null) {
			return;
		}
		mu.lock();
		try {
			if (closed) {
				return; // TODO: error
			}
			Elem<E> newHead = new Elem<E>(this);
			Elem<E> oldHead = this.head;
			this.head = newHead;
			oldHead.value = value;
			oldHead.next = newHead;
			oldHead.ready.countDown();
		} finally {
			mu.unlock();
		}
	}

	public void close() {
		mu.lock();
		try {
			closed = true;
			head.ready.countDown();
		} finally {
			mu.unlock();
		}
	}

	public Sub<E> subscribe() {
		mu.lock();
		try {
			if (closed) {
				return null; // TODO: error
			}
			Sub<E> sub = new Sub<E>(head);
			head.incRefs();
			return sub;
		} finally {
			mu.unlock();
		}
	}

	private void gc() {
		mu.lock();
		try {
			Elem<E> t = this.tail;
			while (t != null && t.refcount.get() == 0) {
				t = t.next;
			}
			if (t == null) {
				this.tail = this.head;
			}
			else {
				this.tail = t;
			}
		} finally {
			mu.unlock();
		}
	}

	long headCount() {
		mu.lock();
		try {
			return head.refcount.get();
		} finally {
			mu.unlock();
		}
	}

	private static final class Elem<E> {

		private final MultiQueue<E> topic;

		private final CountDownLatch ready = new CountDownLatch(1);

		private E value;

		private Elem<E> next;

		private final AtomicLong refcount = new AtomicLong(0);

		Elem(MultiQueue<E> topic) {
			this.topic = topic;
		}

		private void incRefs() {
			refcount.incrementAndGet();
		}

		private void decRefs() {
			long refs = refcount.decrementAndGet();
			if (refs == 0) {
				topic.gc();
			}
		}
	}

	public static final class Sub<E> {

		private Elem<E> cur;

		public Sub(Elem<E> head) {
			this.cur = head;
		}

		public void unsubscribe() {
			if (cur == null) {
				return;
			}
			cur.decRefs();
			cur = null;
		}

		public E poll() {
			Elem<E> c = this.cur;
			if (c == null) {
				return null; // closed ; TODO: return error
			}
			try {
				c.ready.await();
			} catch (InterruptedException e) {
				unsubscribe();
				return null;
			}
			E value = c.value;
			if (value == null) {
				unsubscribe();
				return null;
			}
			Elem<E> next = c.next;
			next.incRefs();
			c.decRefs();
			this.cur = next;
			return value;
		}
	}
}
