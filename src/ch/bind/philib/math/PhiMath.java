package ch.bind.philib.math;

public final class PhiMath {

	private PhiMath() {
	}

	/**
	 * Calculate the sum of all values from 1 to <code>end</code>, including.
	 * That is: <code>sum = 1 + 2 + 3 + ... + (end-1) + end</code> <br/>
	 * Examples:<br/>
	 * <code>
	 * f(0) = 0<br/>
	 * f(1) = 1<br/>
	 * f(2) = 3<br/>
	 * f(3) = 6<br/>
	 * f(10) = 55<br/>
	 * f(100) = 5050<br/>
	 * </code>
	 * 
	 * @param end
	 *            The end value of the sum-range.
	 * @return The sum of all values from 1 to <code>end</code>, including.
	 */
	public static long sumOfRange(long end) {
		if (end < 1)
			return 0;
		// XXX: (end/2) * (end+1) would be possible as well, but for end=1 the
		// term end/2 would result in 0 and therefore be wrong.
		return (end * (end + 1)) / 2;
	}

	/**
	 * Calculates the sum of all values from <code>start</code> to
	 * <code>end</code>, including.
	 * 
	 * @param start
	 *            The start value of the sum-range.
	 * @param end
	 *            The end value of the sum-range.
	 * @return The sum of all values from <code>start</code> to <code>end</code>
	 *         , including.
	 */

	public static long sumOfRange(long start, long end) {
		if (start > end)
			return 0;
		return sumOfRange(end) - sumOfRange(start - 1);
	}

	// TODO
	// public static long sumOfRange(long start, long end, long increment) {
	// if (start > end)
	// throw new ArithmeticException("start is bigger then end");
	// if (start % increment)
	// }

}
