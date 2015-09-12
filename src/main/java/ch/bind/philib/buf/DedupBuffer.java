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

/**
 * DedupBuffer is the interface for a message deduplication buffer.
 *
 * @author philipp meinen
 */
public interface DedupBuffer {

	/**
	 * adds a message to the deduplication buffer. it returns whether the
	 * message was newly added or not.
	 * @param data the data which has to be checked against the current content
	 *            of the deduplication buffer.
	 * @return {@code true} if the message was newly added and is therefore not
	 *         a duplicate. {@code false} if the message was not added to the
	 *         buffer because it is a duplicate
	 */
	boolean add(byte[] data);

	/**
	 * @return the current number of objects in the deduplication buffer.
	 */
	int size();

}
