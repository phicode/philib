package ch.bind.philib.io;

public final class Ring<T> {

	private static final int INITIAL_RING_LEN = 4;

	private static final int RING_LEN_ENHANCING_FACTOR = 2;

	private int off;

	private int size;

	private Object[] ring;

	public void add(T value) {
		if (value == null) {
			return;
		}
		ensureRingSpace();
		int addPos = (off + size) % ring.length;
		ring[addPos] = value;
		size++;
	}

	public T get() {
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
