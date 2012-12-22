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

import ch.bind.philib.lang.ArrayUtil;

public final class ByteArrayManager implements ObjectManager<byte[]> {

	private final int bufferSize;

	public ByteArrayManager(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@Override
	public byte[] create() {
		return new byte[bufferSize];
	}

	@Override
	public boolean prepareForRecycle(byte[] buf) {
		if (buf.length == bufferSize) {
			ArrayUtil.memsetZero(buf);
			return true;
		}
		return false;
	}

	@Override
	public void release(byte[] buf) {
		// the gc will take care of this :)
	}

	@Override
	public boolean canReuse(byte[] buf) {
		return true;
	}
}
