/*
 * Copyright (c) 2006 Philipp Meinen <philipp@bind.ch>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.bind.philib.cache;

import static org.testng.Assert.fail;

import java.util.Random;

import ch.bind.philib.TestUtil;

public final class Benchmark {

	private Benchmark() {}

	private static final int COUNT = 512 * 1024;

	private static final int LOOPCOUNT = 1024 * 1024 * 8;

	private static final int NCPU = Runtime.getRuntime().availableProcessors();

	// TODO: read/write ratios
	// 99/1%
	// 95/5%
	// 90/10%
	// 85/15%
	// 80/20%
	// 70/30%
	// ...

	private static void simple() {
		Cache<Integer, String> cache = new LruCache<Integer, String>(COUNT);
		TestUtil.gcAndSleep();
		benchNormal(cache);
	}

	private static void staged() {
		Cache<Integer, String> cache = new StagedLruCache<Integer, String>(COUNT);
		TestUtil.gcAndSleep();
		benchNormal(cache);
	}

	private static void line() {
		Cache<Integer, String> cache = new LineCache<Integer, String>(COUNT, 4);
		TestUtil.gcAndSleep();
		benchNormal(cache);
	}

	private static void parallelSimple() {
		Cache<Integer, String> cache = new LruCache<Integer, String>(COUNT);
		TestUtil.gcAndSleep();
		benchThreaded(cache, NCPU);
	}

	private static void parallelStaged() {
		Cache<Integer, String> cache = new StagedLruCache<Integer, String>(COUNT);
		TestUtil.gcAndSleep();
		benchThreaded(cache, NCPU);
	}

	private static void parallelLine() {
		Cache<Integer, String> cache = new LineCache<Integer, String>(COUNT, 4);
		TestUtil.gcAndSleep();
		benchThreaded(cache, NCPU);
	}

	private static void benchNormal(Cache<Integer, String> cache) {
		Runner r = new Runner(cache, LOOPCOUNT);
		r.run();
		printStats(r.time, r.hits, r.misses);
	}

	private static void benchThreaded(Cache<Integer, String> cache, int num) {
		Runner[] rs = new Runner[num];
		Thread[] ts = new Thread[num];
		for (int i = 0; i < num; i++) {
			rs[i] = new Runner(cache, LOOPCOUNT / num);
			ts[i] = new Thread(rs[i]);
		}
		long tStart = System.currentTimeMillis();
		for (Thread t : ts)
			t.start();
		try {
			for (Thread t : ts)
				t.join();
		} catch (InterruptedException e) {
			fail("interrupted");
			return;
		}
		final long tEnd = System.currentTimeMillis();
		final long realTime = tEnd - tStart;
		// int time = 0;
		int hit = 0;
		int miss = 0;
		for (Runner r : rs) {
			// time += r.time;
			hit += r.hits;
			miss += r.misses;
		}

		printStats(realTime, hit, miss);
	}

	private static class Runner implements Runnable {

		private final Cache<Integer, String> cache;

		public long time;

		public int hits;

		public int misses;

		private final int loops;

		private final Random random = new Random();

		Runner(Cache<Integer, String> cache, int loops) {
			this.cache = cache;
			this.loops = loops;
		}

		@Override
		public void run() {
			final int queryMax = COUNT * 11 / 10;
			// final int queryMax = COUNT;
			final long tStart = System.currentTimeMillis();
			for (int i = 0; i < loops; i++) {
				Integer rand = random.nextInt(queryMax);
				String v = cache.get(rand);
				if (v != null) {
					hits++;
				} else {
					misses++;
					cache.set(rand, Integer.toString(rand));
				}
			}
			final long tEnd = System.currentTimeMillis();
			time = tEnd - tStart;
		}
	}

	private static void printStats(long time, int hit, int miss) {
		System.out.println("time: " + time + "ms");
		double hps = hit / (time / 1000.0);
		double mps = miss / (time / 1000.0);
		System.out.println("hits: " + hit + " = " + (int) hps + "/sec");
		System.out.println("miss: " + miss + " = " + (int) mps + "/sec");
		System.out.println();
	}

	public static void main(String[] args) {
		simple();
		staged();
		line();
		parallelSimple();
		parallelStaged();
		parallelLine();
	}
}
