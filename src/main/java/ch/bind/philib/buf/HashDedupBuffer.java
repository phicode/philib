/*
 * Copyright (c) 2015 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.buf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import ch.bind.philib.math.Calc;

public class HashDedupBuffer implements DedupBuffer {

	public static final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";

	// must be a power of two for the bucket index mask
	// must also be <= 256 because the first byte of the resulting hash is used
	// to look up the target bucket
	private static final int DEFAULT_NUM_BUCKETS = 16;

	private static final Object VALUE_DUMMY = new Object();

	private final int bucketIndexMask;

	private final int maxBucketSize;

	// synchronization happens per bucket
	private final LinkedHashMap<ByteArrayKey, Object>[] buckets;

	private final MessageDigestThreadLocal messageDigestThreadLocal;

	public HashDedupBuffer(int dedupBufferSize) {
		this(dedupBufferSize, DEFAULT_NUM_BUCKETS, DEFAULT_DIGEST_ALGORITHM);
	}

	@SuppressWarnings("unchecked")
	public HashDedupBuffer(int dedupBufferSize, int numBuckets, String digestAlgorithm) {
		if (numBuckets < 1 || numBuckets > 256 || Integer.bitCount(numBuckets) != 1) {
			throw new IllegalArgumentException("numBuckets must be a power of 2 in the range 1-256 (inclusive)");
		}
		bucketIndexMask = numBuckets - 1;
		maxBucketSize = Calc.ceilDiv(dedupBufferSize, numBuckets);
		buckets = new LinkedHashMap[numBuckets];
		for (int i = 0; i < numBuckets; i++) {
			buckets[i] = new LinkedHashMap<>( //
					maxBucketSize * 3 / 2, // initial capacity
					1f, // load factor
					true // access order
			);
		}
		messageDigestThreadLocal = new MessageDigestThreadLocal(digestAlgorithm);
	}

	@Override
	public boolean add(byte[] data) {
		MessageDigest digest = messageDigestThreadLocal.get();
		byte[] hash = digest.digest(data);

		ByteArrayKey key = new ByteArrayKey(hash);
		int bucketIdx = hash[0] & bucketIndexMask;
		LinkedHashMap<ByteArrayKey, Object> bucket = buckets[bucketIdx];
		synchronized (bucket) {
			if (bucket.put(key, VALUE_DUMMY) != null) {
				// duplicate
				return false;
			}

			// remove old entries
			if (bucket.size() > maxBucketSize) {
				bucket.remove(bucket.keySet().iterator().next());
			}

			return true;
		}
	}

	@Override
	public int size() {
		int size = 0;
		for (LinkedHashMap<?, ?> bucket : buckets) {
			synchronized (bucket) {
				size += bucket.size();
			}
		}
		return size;
	}

	private static class ByteArrayKey {

		private final byte[] data;

		private final int hashCode;

		public ByteArrayKey(byte[] data) {
			this.data = data;
			this.hashCode = Arrays.hashCode(data);
		}

		@Override
		public boolean equals(Object obj) {
			ByteArrayKey o = (ByteArrayKey) obj;
			return Arrays.equals(this.data, o.data);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	private static class MessageDigestThreadLocal extends ThreadLocal<MessageDigest> {

		private final String algorithm;

		public MessageDigestThreadLocal(String algorithm) {
			// ensure 'algorithm' refers to a valid digest
			try {
				MessageDigest.getInstance(algorithm);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("digest algorithm not available: " + algorithm);
			}
			this.algorithm = algorithm;
		}

		@Override
		public void set(MessageDigest value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public MessageDigest get() {
			MessageDigest digest = super.get();
			digest.reset();
			return digest;
		}

		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance(algorithm);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("digest algorithm disappeared ?!?! " + algorithm);
			}
		}
	}
}
