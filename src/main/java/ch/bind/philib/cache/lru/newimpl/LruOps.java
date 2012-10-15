package ch.bind.philib.cache.lru.newimpl;

final class LruOps {

	static void add(final LruRoot root, final LruNode node) {
		assert (node.getPrev() == null && node.getNext() == null);

		final LruNode oldHead = root.getHead();
		if (oldHead == null) {
			assert (root.getTail() == null);
			// empty LRU
			root.setHead(node);
			root.setTail(node);
		}
		else {
			assert (root.getTail() != null && oldHead.getPrev() == null);
			// non-empty LRU
			node.setNext(oldHead);
			oldHead.setPrev(node);
			root.setHead(node);
		}
	}

	static void remove(final LruRoot root, final LruNode node) {
		assert (root.getHead() != null && root.getTail() != null);

		final LruNode head = root.getHead();
		final LruNode tail = root.getTail();
		final LruNode prev = node.getPrev();
		final LruNode next = node.getNext();

		if (head == node) {
			if (tail == node) {
				assert (next == null && prev == null);
				// this was the only element in the LRU
				root.setHead(null);
				root.setTail(null);
			}
			else {
				assert (prev == null && next.getPrev() == node);
				// node is at the head of the LRU
				root.setHead(next);
				next.setPrev(null);
			}
		}
		else {
			if (tail == node) {
				assert (next == null && prev.getNext() == node);
				// node is at the tail of the LRU
				root.setTail(prev);
				prev.setNext(null);
			}
			else {
				assert (prev != null && next != null && prev.getNext() == node && next.getPrev() == node);
				// node is is the middle of the LRU
				prev.setNext(next);
				next.setPrev(prev);
			}
		}
	}

	final void moveToHead(final LruRoot root, final LruNode node) {
		assert (root.getHead() != null && root.getTail() != null);

		final LruNode head = root.getHead();
		if (head == node) {
			// 1 element LRU or already in head position
			return;
		}
		final LruNode tail = root.getTail();
		final LruNode prev = node.getPrev();
		final LruNode next = node.getNext();

		// since this node is not the head there are 2 or more elements in the
		// LRU

		if (tail == node) {
			assert (prev != null && next == null && prev.getNext() == node);
			// move from tail to head

			// unlink from tail
			node.setPrev(null);
			prev.setNext(null);
			root.setTail(prev);

			// fix old-head
			head.setPrev(node);
			node.setNext(head);
			root.setHead(node);
		}
		else {
			assert (prev != null && next != null && prev.getNext() == node && next.getPrev() == node);
			// node is is the middle of the LRU
			// unlink
			prev.setNext(next);
			next.setPrev(prev);

			root.setHead(node);
			node.setNext(head);
			node.setPrev(null);
			head.setPrev(node);
		}
	}
}
