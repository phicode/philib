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

import java.io.PrintStream;

public final class Bench {

	private static final long DEFAULT_MAX_RUNTIME_MS = 2000;

	public static Result run(Bencher bencher) throws InterruptedException {
		return run(bencher, DEFAULT_MAX_RUNTIME_MS);
	}

	public static Result run(Bencher bencher, long minRuntimeMs) throws InterruptedException {
		Validation.notNull(bencher);
		long maxRuntimeNs = minRuntimeMs * 1000000;

		long loops = 10;
		while (true) {
			long timeNs = runAndMeasure(bencher, loops);
			if (timeNs >= maxRuntimeNs) {
				return new Result(bencher, loops, timeNs);
			}
			if (timeNs * 8 < maxRuntimeNs) {
				loops *= 8;
			} else {
				loops *= 2;
			}
		}
	}

	public static void runAndPrint(Bencher bencher) throws InterruptedException {
		run(bencher, DEFAULT_MAX_RUNTIME_MS).print(System.out);
	}

	public static void runAndPrint(Bencher bencher, long minRuntimeMs) throws InterruptedException {
		run(bencher, minRuntimeMs).print(System.out);
	}

	private static long runAndMeasure(Bencher bench, long loops) {
		long t = System.nanoTime();
		bench.run(loops);
		return System.nanoTime() - t;
	}

	public static final class Result {

		private final Bencher bencher;
		private final long loops;
		private final long timeNs;

		public Result(Bencher bencher, long loops, long timeNs) {
			this.bencher = bencher;
			this.loops = loops;
			this.timeNs = timeNs;
		}

		public Bencher getBencher() {
			return bencher;
		}

		public long getLoops() {
			return loops;
		}

		public long getTimeNs() {
			return timeNs;
		}

		public void print(PrintStream printStream) {
			printStream.printf("%-30s %10d ops in %10d ns => %8dns/op\n", //
					bencher.getName(), loops, timeNs, Calc.ceilDiv(timeNs, loops));
		}
	}
}
