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

package ch.bind.philib.cache;

import ch.bind.philib.lang.Cloner;
import org.testng.annotations.Test;

/**
 * @author Philipp Meinen
 */
@Test
public class LineCacheTest extends CacheTestBase {

	@Override
	<K, V> Cache<K, V> create() {
		return new LineCache<K, V>();
	}

	@Override
	<K, V> Cache<K, V> create(int capacity) {
		return new LineCache<K, V>(capacity, 4);
	}

	@Override
	<K, V> Cache<K, V> create(Cloner<V> valueCloner) {
		return new LineCache<K, V>(valueCloner);
	}

	@Override
	int getBucketSize() {
		return LineCache.DEFAULT_ORDER;
	}

	@Override
	int getMinCapacity() {
		return LineCache.DEFAULT_ORDER;
	}

	@Override
	int getDefaultCapacity() {
		return Cache.DEFAULT_CAPACITY;
	}
}
