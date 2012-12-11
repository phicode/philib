package ch.bind.philib.util;

import ch.bind.philib.lang.CompareUtil;
import ch.bind.philib.lang.HashUtil;

public final class Pair<A, B> {

	private final A first;

	private final B second;

	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

	@Override
	public String toString() {
		return String.format("%s[first=%s, second=%s]", getClass().getSimpleName(), first, second);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Pair) {
			final Pair o = (Pair) obj;
			return CompareUtil.equals(this.first, o.first) && //
					CompareUtil.equals(this.second, o.second);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int h = HashUtil.startHash(first);
		return HashUtil.nextHash(h, first);
	}
}
