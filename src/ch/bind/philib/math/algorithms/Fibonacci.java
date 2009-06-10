package ch.bind.philib.math.algorithms;

public final class Fibonacci {

	private long lastFib = 0;
	private long curFib = 1;

	public Fibonacci() {
	}

	public long nextFib() {
		long cur = lastFib + curFib;
		lastFib = curFib;
		curFib = cur;
		return cur;
	}
}
