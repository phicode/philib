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

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class BitStreamEncoder {

	private static final int DEFAULT_INITIAL_SIZE = 16;

	private byte[] encoded = new byte[DEFAULT_INITIAL_SIZE];

	private int encodedSize;

	private long active;

	private int bitsLeft;

	public BitStreamEncoder() {
		// this.data = new byte[DEFAULT_INITIAL_SIZE];
		// this.capacity = DEFAULT_INITIAL_SIZE * 8;
		bitsLeft = 64;
	}

	public void writeByte(int value) {
		if (bitsLeft < 8) {
			// flush();
		}
		// active = ((active << 8) | (value & MASK[8]));
	}

	private void ensureSizeBytes(int num) {
		int len = encoded.length;
		int rem = len - encodedSize;
		if (rem < num) {
			byte[] enc = new byte[len * 2];
			encoded = ac(encoded, enc, len);
		}
	}

	private static byte[] ac(byte[] src, byte[] dst, int len) {
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}

	private void finishEncode() {

	}
}
