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

	public static long calcFib(final int num) {
		long last = 1;
		long cur = 1;
		for (int i = 1; i < num; i++) {
			long newCur = last + cur;
			last = cur;
			cur = newCur;
		}
		return cur;
	}
}
