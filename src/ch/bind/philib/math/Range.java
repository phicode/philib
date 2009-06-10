package ch.bind.philib.math;

public final class Range {

	private final long start;
	private final long end;
	private final long increment;

	public Range(long end) {
		this(1, end, 1);
	}

	public Range(long start, long end) {
		this(start, end, 1);
	}

	public Range(long start, long end, long increment) {
		this.start = start;
		this.end = end;
		this.increment = increment;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public long getIncrement() {
		return increment;
	}
}
