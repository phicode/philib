package ch.bind.philib.math;

public abstract class PhiMath {
	
	protected PhiMath() {}
	
	public static long ceilDiv(long num, long divisor) {
		long res = num / divisor;
		if (res*divisor< num) {
			res++;
		}
		return res;
	}
}
