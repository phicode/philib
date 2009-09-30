package ch.bind.philib;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PhiArraysTest {

	@Test(expected = NullPointerException.class)
	public void sourceNull() {
		Object[] arr = new Object[1];

		PhiArrays.pickRandom(null, arr);
	}

	@Test(expected = NullPointerException.class)
	public void destinationNull() {
		Object[] arr = new Object[1];
		PhiArrays.pickRandom(arr, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void sourceSmallerThenDestination() {
		Object[] src = new Object[1];
		Object[] dst = new Object[2];
		PhiArrays.pickRandom(src, dst);
	}

	@Test
	public void equalSize() {
		final int N = 4096;
		Integer[] src = new Integer[N];
		Integer[] dst = new Integer[N];
		boolean[] found = new boolean[N];
		for (int i = 0; i < N; i++) {
			src[i] = i;
		}
		PhiArrays.pickRandom(src, dst);
		for (int i = 0; i < N; i++) {
			int v = dst[i].intValue();
			assertTrue(v >= 0);
			assertTrue(v < N);
			assertFalse(found[v]);
			found[v] = true;
		}
	}
}
