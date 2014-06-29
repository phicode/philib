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

package ch.bind.philib.pool;

/**
 * The base interface for object pools.
 *
 * @param <T> The type of object which are managed by this pool.
 * @author Philipp Meinen
 */
public interface Pool<T> {

	/**
	 * Take an object from the object-pool.
	 *
	 * @return A free and usable object from the pool if one exists. Otherwise a new object is created from the
	 * underlying manager.
	 */
	T take();

	/**
	 * Recycles an object which might be reused. The caller must not use this object after calling this method.
	 *
	 * @param value The object to be recycled.
	 */
	void recycle(T value);

	/**
	 * @return This pool's statistics object. Calls to this pool's methods are visible to successive calls to this
	 * statics-object's methods.
	 */
	PoolStats getPoolStats();

	/**
	 * @return The number of currently pooled objects.
	 */
	int getNumPooled();

	/**
	 * instructs the pool to destroy all pooled objects.
	 */
	void clear();
}
