package ch.bind.philib.lang;

import java.util.Random;

public class MurmurHashTest {

	private static final Random r = new Random();

	public static void main(String[] args) {
		for (int i = 0; i < 4; i++) {
			testSpeed(1);
			testSpeed(2);
			testSpeed(3);
			testSpeed(4);
		}
		int size = 8;
		for (int i = 0; i < 14; i++) {
			testSpeed(size);
			size *= 2;
		}
	}

	private static void testSpeed(final int size) {
		byte[] b = new byte[size];
		r.nextBytes(b);
		long total = 0;
		long sum = 0;
		long tStart = System.nanoTime();
		final long process = 1 << 30;
		final long bucket = size * 8;
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
		System.out.printf("size=%5d total=%6d sum=%20d %5.3fms %3.3fmb/sec %.3fns/byte%n", size, total, sum, (time / 1000000f), mbPerSec, nsPerByte);
	}
}
