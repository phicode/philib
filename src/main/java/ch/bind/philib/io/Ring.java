package ch.bind.philib.io;

import ch.bind.philib.validation.SimpleValidation;

public class Ring<T> {

	private static final int INITIAL_RING_LEN = 4;

	private static final int RING_LEN_ENHANCING_FACTOR = 2;

	private int off;

	private int size;

	private Object[] ring;

	public void addBack(T value) {
		if (value == null) {
			return;
		}
		ensureRingSpace();
		int addPos = (off + size) % ring.length;
		ring[addPos] = value;
		size++;
	}

	public void addFront(T value) {
		if (value == null) {
			return;
		}
		ensureRingSpace();
		off--;
		if (off == -1) {
			off = ring.length - 1;
		}
		ring[off] = value;
		size++;
	}

	@SuppressWarnings("unchecked")
	public T poll() {
		if (size == 0) {
			return null;
		}
		else {
			Object value = ring[off];
			size--;
			off = (off + 1) % ring.length;
			return (T) value;
		}
	}

	public T pollNext(T value) {
		SimpleValidation.notNull(value);
		if (size == 0) {
			return value;
		}
		else {
			T rv = poll();
			addBack(value);
			return rv;
		}
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	private void ensureRingSpace() {
		if (ring == null) {
			ring = new Object[INITIAL_RING_LEN];
		}
		else {
			if (size == ring.length) {
				int newLen = ring.length * RING_LEN_ENHANCING_FACTOR;
				if (newLen < 0) {
					// TODO
					System.out.println("overflow!");
					newLen = Integer.MAX_VALUE;
				}
				Object[] newRing = new Object[newLen];
				System.arraycopy(ring, 0, newRing, 0, ring.length);
				this.ring = newRing;
			}
		}
	}

}
