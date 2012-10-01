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

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class BufferOps {

	private BufferOps() {
	}

	private static volatile byte[] nullFiller;

	public static void memsetZero(ByteBuffer buf) {
		if (buf.hasArray()) {
			memsetZero(buf.array());
		} else {
			byte[] filler = getFiller();
			int filLen = filler.length;
			buf.clear();
			int rem = buf.remaining();
			while (rem > 0) {
				int l = Math.min(rem, filLen);
				buf.put(filler, 0, l);
				rem -= l;
			}
			buf.clear();
		}
	}

	public static void memsetZero(byte[] buf) {
		byte[] filler = getFiller();
		int filLen = filler.length;
		int rem = buf.length;
		int off = 0;
		while (rem > 0) {
			int l = Math.min(rem, filLen);
			memset(filler, buf, off, l);
			rem -= l;
			off += l;
		}
	}

	private static final void memset(byte[] src, byte[] dst, int dstOff, int len) {
		System.arraycopy(src, 0, dst, dstOff, len);
	}

	private static byte[] getFiller() {
		byte[] f = nullFiller;
		if (f == null) {
			f = new byte[8192];
			nullFiller = f;
		}
		return f;
	}
}
