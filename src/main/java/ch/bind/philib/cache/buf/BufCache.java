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

package ch.bind.philib.cache.buf;

/**
 * The base interface for buffer caches.
 * 
 * @author Philipp Meinen
 * 
 * @param <E> The type of buffers which are managed by this cache.
 */
public interface BufCache<E> {

	/**
	 * Acquire an object from the object-cache.
	 * 
	 * @return A free and usable buffer from the cache if one exists. Otherwise
	 *         a new buffer is created from the underlying factory.
	 */
	E acquire();

	/**
	 * Free a buffer to the cache. The caller must not use this buffer after
	 * calling this method.
	 * 
	 * @param e The buffer to be released.
	 */
	void free(E e);

	/**
	 * @return The cache-statistics object for this buffer-cache.
	 *         {@code acquire} and {@code free} calls to the cache are visible
	 *         to successive calls to this statics-object's methods.
	 */
	Stats getCacheStats();

}
