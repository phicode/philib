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

package ch.bind.philib.cache;

final class StagedCacheEntry<K, V> extends SimpleCacheEntry<K, V> {

	private static final int TOGGLE_OLD_GEN_BIT = 0x40000000;

	private static final int NO_OLD_GEN_BITMASK = 0x3FFFFFFF;

	// bits 0-29 are for the hit-counter, bit 30 is the old-gen toggle
	// and bit 31 is the 'unused' sign-extension
	private int hits;

	StagedCacheEntry(K key, V value) {
		super(key, value);
	}

	int recordHit() {
		// hits are only recorded for young-generation objects
		// so we do not have to worry about an integer overflow
		// additionally the hits are reset to zero once an entry
		// moves back down from the old generation
		return (++hits & NO_OLD_GEN_BITMASK);
	}

	void resetHits() {
		hits = hits & TOGGLE_OLD_GEN_BIT;
	}

	boolean isInYoungGen() {
		return (hits & TOGGLE_OLD_GEN_BIT) == 0;
	}

	void setInYoungGen() {
		hits = (hits & NO_OLD_GEN_BITMASK);
	}

	void setInOldGen() {
		hits = (hits | TOGGLE_OLD_GEN_BIT);
	}
}
