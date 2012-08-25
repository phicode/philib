package ch.bind.philib.util;

public final class NoOpLoadAvg implements LoadAvg {

	public static final NoOpLoadAvg INSTANCE = new NoOpLoadAvg();

	@Override
	public void start() {
	}

	@Override
	public void end() {
	}

	@Override
	public long getLoadAvg() {
		return 0;
	}

	@Override
	public double getLoadAvgAsFactor() {
		return 0f;
	}
}
