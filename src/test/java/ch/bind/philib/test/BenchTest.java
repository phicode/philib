/*
 * Copyright (c) 2014 Philipp Meinen <philipp@bind.ch>
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
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class BenchTest {

	@Test
	public void benchTest() throws InterruptedException {
		Bencher bencher = new Bencher() {
			@Override
			public void run(long loops) {
				long s = 0;
				for (long i = 1; i <= loops; i++) {
					s += Math.max(0, i);
				}
				assertEquals(s, Calc.sumOfRange(loops));
			}

			@Override
			public String getName() {
				return getClass().getSimpleName();
			}
		};
		long minRuntimeMs = 200;
		Bench.Result result = Bench.run(bencher, minRuntimeMs);
		assertSame(result.getBencher(), bencher);
		assertTrue(result.getLoops() > 0);
		assertTrue(result.getTimeNs() >= minRuntimeMs);
	}
}