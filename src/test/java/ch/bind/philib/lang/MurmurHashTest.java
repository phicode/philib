/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
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
package ch.bind.philib.lang;

import java.util.Random;

public class MurmurHashTest {

	private static final Random r = new Random();

	public static void main(String[] args) {
		System.out.printf("optimized in: %dns%n", MurmurHash.optimize());
		for (int i = 0; i < 2; i++) {
			testSpeed2(1);
			testSpeed2(2);
			testSpeed2(3);
			testSpeed2(4);
			testSpeed3(1);
			testSpeed3(2);
			testSpeed3(3);
			testSpeed3(4);
		}
		int size = 8;
		for (int i = 0; i < 14; i++) {
			testSpeed2(size);
			size *= 2;
		}
		size = 8;
		for (int i = 0; i < 14; i++) {
			testSpeed3(size);
			size *= 2;
		}
	}

	private static void testSpeed2(final int size) {
		byte[] b = new byte[size];
		r.nextBytes(b);
		long total = 0;
		long sum = 0;
		long tStart = System.nanoTime();
		final long process = 1 << 30;
		final long bucket = size * 8L;
		while ((total + bucket) < process) {
			sum += MurmurHash.murmur2(b);
			sum += MurmurHash.murmur2(b);
			sum += MurmurHash.murmur2(b);
			sum += MurmurHash.murmur2(b);
			sum += MurmurHash.murmur2(b);
			sum += MurmurHash.murmur2(b);
			sum += MurmurHash.murmur2(b);
			sum += MurmurHash.murmur2(b);
			total += bucket;
		}
		while (total < process) {
			sum += MurmurHash.murmur2(b);
			total += size;
		}
		long time = System.nanoTime() - tStart;
		double mbPerSec = total / (time / 1000000000f) / (1024f * 1024f);
		double nsPerByte = ((double) time) / ((double) total);
		System.out.printf("murmur2 size=%5d total=%6d sum=%20d %5.3fms %3.3fmb/sec %.3fns/byte%n", //
				size, total, sum, (time / 1000000f), mbPerSec, nsPerByte);
	}

	private static void testSpeed3(final int size) {
		byte[] b = new byte[size];
		r.nextBytes(b);
		long total = 0;
		long sum = 0;
		long tStart = System.nanoTime();
		final long process = 1 << 30;
		final long bucket = size * 8L;
		while ((total + bucket) < process) {
			sum += MurmurHash.murmur3(b);
			sum += MurmurHash.murmur3(b);
			sum += MurmurHash.murmur3(b);
			sum += MurmurHash.murmur3(b);
			sum += MurmurHash.murmur3(b);
			sum += MurmurHash.murmur3(b);
			sum += MurmurHash.murmur3(b);
			sum += MurmurHash.murmur3(b);
			total += bucket;
		}
		while (total < process) {
			sum += MurmurHash.murmur3(b);
			total += size;
		}
		long time = System.nanoTime() - tStart;
		double mbPerSec = total / (time / 1000000000f) / (1024f * 1024f);
		double nsPerByte = ((double) time) / ((double) total);
		System.out.printf("murmur3 size=%5d total=%6d sum=%20d %5.3fms %3.3fmb/sec %.3fns/byte%n", //
				size, total, sum, (time / 1000000f), mbPerSec, nsPerByte);
	}
}
