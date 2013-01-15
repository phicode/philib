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
package ch.bind.philib.pool.manager;

/**
 * A manager for objects which support pooling.
 * 
 * @author Philipp Meinen
 */
public interface ObjectManager<T> {

	T create();

	void release(T value);

	/**
	 * Prepare an object to be reused by a different user. Implementors of this method must make sure that any data from
	 * previous users is cleared.
	 * 
	 * @param value The object which must be prepared for reuse.
	 * @return {@code true} if this object can be reused, {@code false} otherwise (for example if the size of the
	 *         offered buffer is too small). After returning {@code false} {@link destroy(T)} will be called.
	 */
	boolean prepareForRecycle(T value);

	/**
	 * @param value -
	 * @return {@code true} if this pooled object can be reused, {@code false} otherwise. Example: The pooled object is
	 *         a database connection which might not be reusable because the connection was closed while being in the
	 *         pool.
	 */
	boolean canReuse(T value);

}
