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

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.bind.philib.TestUtil;

public class BenchMutex {

	private static final long N = 200000000;

	private static int m;

	public static void main(String[] args) throws Exception {
		for (int i = 1; i <= 16; i++) {
			m = 0;
			Semaphore start = new Semaphore(0);
			Semaphore stop = new Semaphore(0);
			Lock mutex = new ReentrantLock();
			long n = N / i;

			for (int j = 0; j < i; j++) {
				startThread(n, mutex, start, stop);
			}

			TestUtil.gcAndSleep(1000);
			long tstart = System.nanoTime();
			start.release(i);
			stop.acquire(i);
			long tend = System.nanoTime();
			long t = tend - tstart;
			double avg = ((double) t) / ((double) N);
			System.out.printf("%d: %.3fns, m:%d\n", i, avg, m);
		}
	}

	private static void startThread(final long n, final Lock mutex, final Semaphore start, final Semaphore stop) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					start.acquire();
				} catch (InterruptedException e) {
					System.out.println("interrupted");
					System.exit(1);
				}
				for (int i = 0; i < n; i++) {
					mutex.lock();
					try {
						BenchMutex.m++;
					} finally {
						mutex.unlock();
					}
				}
				stop.release();
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
}
