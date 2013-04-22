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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.mockito.Mockito;
import org.testng.annotations.Test;

public class SyncCacheTest {

	@Test
	public void checkPassThrough() {
		Cache<Integer, Integer> cache = Mockito.mock(Cache.class);
		Cache<Integer, Integer> sync = SyncCache.wrap(cache);

		sync.set(1, 2);
		Mockito.verify(cache).set(1, 2);

		Mockito.when(cache.get(1)).thenReturn(2);
		Integer v = sync.get(1);
		assertNotNull(v);
		assertEquals(v.intValue(), 2);
		Mockito.verify(cache).get(1);

		sync.remove(99);
		Mockito.verify(cache).remove(99);

		Mockito.when(cache.capacity()).thenReturn(123);
		assertEquals(sync.capacity(), 123);
		Mockito.verify(cache).capacity();

		sync.clear();
		Mockito.verify(cache).clear();

		Mockito.verifyNoMoreInteractions(cache);
	}
}
