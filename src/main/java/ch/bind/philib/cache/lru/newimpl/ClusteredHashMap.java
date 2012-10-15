package ch.bind.philib.cache.lru.newimpl;

final class ClusteredHashMap<K, V, T extends ClusteredHashEntry<K, V>> {

	private final ClusteredHashEntry<K, V>[] table;

	ClusteredHashMap(int capacity) {
		table = new ClusteredHashEntry[capacity];
	}

	boolean add(final T entry) {
		assert (entry != null && entry.getNext() == null && entry.getKey() != null && entry.getValue() != null);

		final int hash = entry.cachedHash();
		final int position = hashPosition(hash);

		ClusteredHashEntry<K, V> now = table[position];
		if (now == null) {
			table[position] = entry;
			return true;
		}
		else {
			final K key = entry.getKey();
			ClusteredHashEntry<K, V> prev = null;
			while (now != null) {
				if (hash == now.cachedHash() && key.equals(now.getKey())) {
					// key is already in the table
					return false;
				}
				prev = now;
				now = now.getNext();
			}
			assert (prev != null);
			prev.setNext(now);
			return true;
		}
	}

	boolean remove(final T entry) {
		assert (entry != null);

		final K key = entry.getKey();
		final int hash = entry.cachedHash();
		final int position = hashPosition(hash);

		ClusteredHashEntry<K, V> prev = null;
		ClusteredHashEntry<K, V> now = table[position];
		while (now != null && now != entry) {
			prev = now;
			now = now.getNext();
		}
		if (now != null) {
			assert (hash == now.cachedHash() && key.equals(now.getKey()));
			if (prev == null) {
				// first entry in the table
				table[position] = now.getNext();
			}
			else {
				// there are entries before this one
				prev.setNext(now.getNext());
			}
			return true; // entry found and removed
		}
		return false; // entry not found
	}

	// returns null if a pair does not exist
	T get(final K key) {
		assert (key != null);

		final int hash = key.hashCode();
		final int position = hashPosition(hash);

		ClusteredHashEntry<K, V> entry = table[position];
		while (entry != null && hash != entry.cachedHash() && key.equals(entry.getKey()) == false) {
			entry = entry.getNext();
		}
		return (T) entry;
	}

	private int hashPosition(int hash) {
		int p = hash % table.length;
		return Math.abs(p);
	}
}
