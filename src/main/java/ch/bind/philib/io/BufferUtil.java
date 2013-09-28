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

package ch.bind.philib.io;

import java.nio.ByteBuffer;

public final class BufferUtil {

	private BufferUtil() {
	}

	public static ByteBuffer append(ByteBuffer dst, ByteBuffer src) {
		int cap = dst.capacity();
		int pos = dst.position();
		int lim = dst.limit();

		int inSrc = src.remaining();
		int availableBack = cap - lim;
		if (availableBack >= inSrc) {
			// there is enough room at the end of the buffer, make that room
			// visible
			dst.position(lim);
			dst.limit(cap);
			dst.put(src);
			// update position and limit to reflect the old+new data
			dst.position(pos);
			dst.limit(lim + inSrc);
			return dst;
		}
		int inDst = dst.remaining();
		// there is not enough room, but maybe there is some room in front?
		if (pos != 0 && (pos + availableBack) >= inSrc) {
			dst.compact();
			dst.position(inDst);
			dst.limit(cap);
			dst.put(src);
			dst.flip(); // lim=pos ; pos = 0
			return dst;
		}

		int required = inDst + inSrc;
		// a new, bigger buffer is required
		ByteBuffer buf = dst.isDirect() ? //
		ByteBuffer.allocateDirect(required)
		        : ByteBuffer.allocate(required);
		buf.put(dst);
		buf.put(src);
		buf.flip(); // pos=0; lim=required
		return buf;
	}
}
