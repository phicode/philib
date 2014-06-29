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

package ch.bind.philib.util;

import ch.bind.philib.TestUtil;
import ch.bind.philib.math.Calc;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class QueueBench {

	public static void main(String[] args) throws Exception {
		printHeader();
		testRange(new LinkedBlockingQueue<Integer>());
		testRange(new ArrayBlockingQueue<Integer>(10000));
		testRange(new ConcurrentLinkedQueue<Integer>());
	}

	private static void printHeader() {
		System.out.print("name,");
		for (int np = 1; np <= 5; np++) {
			for (int nc = 1; nc <= 5; nc++) {
				System.out.printf("%dp%dc,", np, nc);
			}
		}
		System.out.println();
	}

	private static void testRange(Queue<Integer> queue) throws Exception {
		final int N = 10000000;
		System.out.print(queue.getClass().getSimpleName() + ',');
		for (int np = 1; np <= 5; np++) {
			for (int nc = 1; nc <= 5; nc++) {
				long t = test(np, nc, N, queue);
				double nsPerOp = ((double) t) / ((double) N);
				System.out.printf("%.3f,", nsPerOp);
			}
		}
		System.out.println();
	}

	private static long test(int nProducer, int nConsumer, int n, Queue<Integer> queue) throws Exception {
		Semaphore ctrl = new Semaphore(0);
		Producer[] ps = new Producer[nProducer];
		Consumer[] cs = new Consumer[nConsumer];
		int workPerP = n / nProducer;
		int workLastP = n - ((nProducer - 1) * workPerP);
		int workPerC = n / nConsumer;
		int workLastC = n - ((nConsumer - 1) * workPerC);

		for (int i = 0, start = 1; i < nProducer; i++, start += workPerP) {
			int work = workPerP;
			if (i == nProducer - 1) {
				work = workLastP;
			}
			ps[i] = new Producer(queue, work, start, ctrl);
			Thread t = new Thread(ps[i], "producer-" + i);
			t.start();
		}
		for (int i = 0; i < nConsumer; i++) {
			int work = workPerC;
			if (i == nConsumer - 1) {
				work = workLastC;
			}
			cs[i] = new Consumer(queue, work, ctrl);
			Thread t = new Thread(cs[i], "consumer-" + i);
			t.start();
		}

		TestUtil.gcAndSleep(2000);

		long tstart = System.nanoTime();
		ctrl.release(nProducer + nConsumer);
		while (ctrl.availablePermits() > 0) {
			Thread.yield();
		}
		ctrl.acquire(nProducer + nConsumer);
		long tend = System.nanoTime();
		long t = tend - tstart;
		// System.out.println("time: " + t);

		long expsum = Calc.sumOfRange(n);
		long gotsum = 0;
		for (Consumer c : cs) {
			gotsum += c.sum;
		}
		if (expsum != gotsum) {
			System.out.printf("sums do not line uf, expected=%d, got=%d\n", expsum, gotsum);
		}
		return t;
	}

	private static final class Producer implements Runnable {

		final Queue<Integer> queue;

		final int num;

		final int start;

		final Semaphore ctrl;

		public Producer(Queue<Integer> queue, int num, int start, Semaphore ctrl) {
			this.queue = queue;
			this.num = num;
			this.start = start;
			this.ctrl = ctrl;
		}

		@Override
		public void run() {
			try {
				ctrl.acquire();
				for (int i = 0, v = start; i < num; i++, v++) {
					Integer vi = Integer.valueOf(v);
					while (!queue.offer(vi)) {
						Thread.yield();
					}
				}
			} catch (InterruptedException e) {
				System.out.println("producer interrupted");
				System.exit(1);
			} finally {
				ctrl.release();
			}
		}
	}

	private static final class Consumer implements Runnable {

		final Queue<Integer> queue;

		final int num;

		final Semaphore ctrl;

		long sum;

		public Consumer(Queue<Integer> queue, int num, Semaphore ctrl) {
			this.queue = queue;
			this.num = num;
			this.ctrl = ctrl;
		}

		@Override
		public void run() {
			try {
				ctrl.acquire();
				for (int i = 0; i < num; i++) {
					Integer v;
					while ((v = queue.poll()) == null) {
						Thread.yield();
					}
					sum += v.intValue();
				}
			} catch (InterruptedException e) {
				System.out.println("consumer interrupted");
				System.exit(1);
			} finally {
				ctrl.release();
			}
		}
	}
}
