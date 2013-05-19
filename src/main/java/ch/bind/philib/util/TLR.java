package ch.bind.philib.util;

import java.util.Random;

/**
 * Thread local random. <br />
 * 
 * @author phil
 * @TODO deprecate in favor of java.util.concurrent.ThreadLocalRandom once
 *       everyone is using java 7.
 */
public final class TLR {
	private TLR() {
	}

	private static ThreadLocal<Random> tlr = new ThreadLocal<Random>() {
		protected Random initialValue() {
			return new Random();
		};
	};

	public static Random current() {
		return tlr.get();
	}
}
