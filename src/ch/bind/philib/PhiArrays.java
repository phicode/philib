package ch.bind.philib;

import java.util.Random;

/**
 * Various functions for dealing with arrays which are not present in the
 * standard {@link java.util.Arrays} class.
 * 
 * @author Philipp Meinen
 * @since 2009-06-10
 */
public final class PhiArrays {

	private static final Random rand = new Random(); // TODO: thread-safe?

	/**
	 * Fills the <code>destination</code> array with randomly picked values from
	 * the <code>source</code> array. No value will be picked twice.
	 * 
	 * @param source
	 *            The array from which random values must be picked. The content
	 *            of this array will not be altered.
	 * @param destination
	 *            The array which must be filled with random values. Previous
	 *            values within this array will be overwritten.
	 * @throws NullPointerException
	 *             If either of the two parameters is null.
	 * @throws IllegalArgumentException
	 *             If the <code>source</code>-array is smaller then the
	 *             <code>destination</code>-array.
	 */
	public static <T> void pickRandom(final T[] source, final T[] destination) {
		if (source == null)
			throw new NullPointerException("the source array must not be null");
		if (destination == null)
			throw new NullPointerException(
					"the destination array must not be null");
		final int nSrc = source.length;
		final int nDst = destination.length;
		if (nSrc < nDst)
			throw new IllegalArgumentException(
					"the source arrays length must be greater or equal to the destination arrays length");
		final boolean[] taken = new boolean[nSrc];

		for (int i = 0; i < nDst; i++) {
			int idx = rand.nextInt(nSrc);
			while (taken[idx])
				idx = rand.nextInt(nSrc);
			taken[idx] = true;
			destination[i] = source[idx];
		}
	}

}
