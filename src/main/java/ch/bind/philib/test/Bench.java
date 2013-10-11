package ch.bind.philib.test;

import ch.bind.philib.math.Calc;
import ch.bind.philib.validation.Validation;

public class Bench {

	private static final long DEFAULT_DESIRED_RUNTIME = 2000;
	private static final long SLEEP_AFTER_GC_MS = 25;

	public static void runBench(Bencher bench) throws InterruptedException {
		runBench(bench, DEFAULT_DESIRED_RUNTIME);
	}

	public static void runBench(Bencher bench, long desiredRuntimeMs) throws InterruptedException {
		Validation.notNull(bench);
		long desiredRuntimeNs = desiredRuntimeMs * 1000000;

		long n = 10;
		while (true) {
			// System.out.println("runnting " + bench.getName() + "(" + n + ")");
			long tNs = runBenchAndMeasure(bench, n);
			if (tNs > desiredRuntimeNs) {
				printStats(bench.getName(), tNs, n);
				return;
			}
			if (tNs < desiredRuntimeNs / 5) {
				n *= 5;
			} else {
				n *= 2;
			}
		}
	}

	private static long runBenchAndMeasure(Bencher bench, long n) throws InterruptedException {
		System.gc();
		Thread.sleep(SLEEP_AFTER_GC_MS);
		long t = System.nanoTime();
		bench.run(n);
		return System.nanoTime() - t;
	}

	private static void printStats(String name, long tNs, long n) {
		System.out.printf("%-30s %10dops in %10dns => %8dns/op\n", name, n, tNs, Calc.ceilDiv(tNs, n));
	}
}
