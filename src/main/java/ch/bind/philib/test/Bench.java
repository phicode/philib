/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.test;

import ch.bind.philib.math.Calc;
import ch.bind.philib.validation.Validation;

public final class Bench {

	private static final long DEFAULT_MAX_RUNTIME_MS = 2000;

	public static void run(Bencher bench) throws InterruptedException {
		run(bench, DEFAULT_MAX_RUNTIME_MS);
	}

	public static void run(Bencher bench, long maxRuntimeMs) throws InterruptedException {
		Validation.notNull(bench);
		long maxRuntimeNs = maxRuntimeMs * 1000000;

		long n = 1;
		while (true) {
			long tNs = runBenchAndMeasure(bench, n);
			if (tNs >= maxRuntimeNs) {
				printStats(bench.getName(), tNs, n);
				return;
			}
			n *= 2;
		}
	}

	private static long runBenchAndMeasure(Bencher bench, long n) throws InterruptedException {
		long t = System.nanoTime();
		bench.run(n);
		return System.nanoTime() - t;
	}

	private static void printStats(String name, long tNs, long n) {
		System.out.printf("%-30s %10d ops in %10d ns => %8dns/op\n", name, n, tNs, Calc.ceilDiv(tNs, n));
	}
}
