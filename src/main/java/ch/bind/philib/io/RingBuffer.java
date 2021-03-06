/*
 * Copyright (c) 2006-2011 Philipp Meinen <philipp@bind.ch>
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

import ch.bind.philib.validation.Validation;

/**
 * @author Philipp Meinen
 */
// TODO: max-capacity
public final class RingBuffer implements DoubleSidedBuffer {

	public static final int DEFAULT_CAPACITY = 8192;

	private byte[] ringBuf;

	// total capacity
	private int ringCapacity;

	// where the data starts
	private int ringOffset;

	// available data
	private int ringSize;

	public RingBuffer() {
		this(DEFAULT_CAPACITY);
	}

	public RingBuffer(int capacity) {
		Validation.notNegative(capacity, "capacity must not be negative");
		init(capacity);
	}

	@Override
	public int available() {
		return ringSize;
	}

	@Override
	public boolean isEmpty() {
		return ringSize == 0;
	}

	@Override
	public int capacity() {
		return ringCapacity;
	}

	@Override
	public void clear() {
		ringOffset = 0;
		ringSize = 0;
	}

	@Override
	public void read(byte[] data) {
		check(data);
		readFront(data, 0, data.length);
	}

	@Override
	public void read(byte[] data, int off, int len) {
		check(data, off, len);
		readFront(data, off, len);
	}

	@Override
	public void readBack(byte[] data) {
		check(data);
		_readBack(data, 0, data.length);
	}

	@Override
	public void readBack(byte[] data, int off, int len) {
		check(data, off, len);
		_readBack(data, off, len);
	}

	@Override
	public void write(byte[] data) {
		check(data);
		_write(data, 0, data.length);
	}

	@Override
	public void write(byte[] data, int off, int len) {
		check(data, off, len);
		_write(data, off, len);
	}

	@Override
	public void writeFront(byte[] data) {
		check(data);
		_writeFront(data, 0, data.length);
	}

	@Override
	public void writeFront(byte[] data, int off, int len) {
		check(data, off, len);
		_writeFront(data, off, len);
	}

	private void readFront(byte[] data, int off, int len) {
		if (len == 0) {
			return;
		}
		readLenCheck(len);
		copyFromRingBufFront(data, off, len);
		consumedFront(len);
	}

	private void _readBack(byte[] data, int off, int len) {
		if (len == 0) {
			return;
		}
		readLenCheck(len);
		copyFromRingBufBack(data, off, len);
		consumedBack(len);
	}

	private void _write(byte[] data, int off, int len) {
		int newSize = ringSize + len;
		_ensureBufferSize(newSize);
		copyToRingBufBack(data, off, len);
		ringSize = newSize;
	}

	private void _writeFront(byte[] data, int off, int len) {
		int newSize = ringSize + len;
		_ensureBufferSize(newSize);
		copyToRingBufFront(data, off, len);
		ringSize = newSize;
		ringOffset = offsetMinus(len);
	}

	private void init(int capacity) {
		this.ringCapacity = capacity;
		this.ringBuf = new byte[capacity];
	}

	private void _ensureBufferSize(int requiredSpace) {
		if (requiredSpace <= ringCapacity) {
			return;
		}
		int newCap = ringCapacity * 2;
		while (newCap < requiredSpace) {
			newCap *= 2;
		}
		byte[] newBuf = new byte[newCap];
		// copy all data into the beginning of the new buffer
		copyFromRingBufFront(newBuf, 0, ringSize);
		this.ringBuf = newBuf;
		this.ringCapacity = newCap;
		this.ringOffset = 0;
	}

	private void copyFromRingBufFront(byte[] buf, int off, int len) {
		int availToEnd = ringCapacity - ringOffset;
		if (availToEnd >= len) {
			// all data is available from one read
			ac(ringBuf, ringOffset, buf, off, len);
		} else {
			// read available space from the offset to the end of the buffer
			// then read the rest of the required data from the beginning
			int rem = len - availToEnd;
			ac(ringBuf, ringOffset, buf, off, availToEnd);
			ac(ringBuf, 0, buf, off + availToEnd, rem);
		}
	}

	private void copyFromRingBufBack(byte[] buf, int off, int len) {
		int firstReadOffset = offsetPlus(ringSize - len);
		int availToEnd = ringCapacity - firstReadOffset;
		int numReadOne = Math.min(availToEnd, len);
		int numReadTwo = len - numReadOne;
		ac(ringBuf, firstReadOffset, buf, off, numReadOne);
		if (numReadTwo > 0) {
			ac(ringBuf, 0, buf, off + numReadOne, numReadTwo);
		}
	}

	private void copyToRingBufBack(byte[] data, int off, int len) {
		int writePosOne = offsetPlus(ringSize);
		int availBack = ringCapacity - writePosOne;
		int numWriteOne = Math.min(availBack, len);
		int numWriteTwo = len - numWriteOne;

		ac(data, off, ringBuf, writePosOne, numWriteOne);
		if (numWriteTwo > 0) {
			ac(data, off + numWriteOne, ringBuf, 0, numWriteTwo);
		}
	}

	private void copyToRingBufFront(byte[] data, int off, int len) {
		int writePosOne = offsetMinus(len);
		int availBack = ringCapacity - writePosOne;
		int numWriteOne = Math.min(availBack, len);
		ac(data, off, ringBuf, writePosOne, numWriteOne);
		int numWriteTwo = len - numWriteOne;
		if (numWriteTwo > 0) {
			ac(data, off + numWriteOne, ringBuf, 0, numWriteTwo);
		}
	}

	private void consumedFront(int len) {
		ringSize -= len;
		if (ringSize == 0) {
			// try to realign the ringbuffer if it is emtpy
			ringOffset = 0;
		} else {
			ringOffset = offsetPlus(len);
		}
	}

	private void consumedBack(int len) {
		ringSize -= len;
		if (ringSize == 0) {
			// try to realign the ringbuffer if it is emtpy
			ringOffset = 0;
		}
	}

	private void readLenCheck(int len) {
		if (this.ringSize < len) {
			throw new IllegalArgumentException("not enough data in buffer");
		}
	}

	private int offsetPlus(int shift) {
		int offset = ringOffset + shift;
		offset %= ringCapacity;
		return offset;
	}

	private int offsetMinus(int shift) {
		int offset = ringOffset - shift;
		if (offset < 0) {
			offset += ringCapacity;
		}
		return offset;
	}

	private static void check(byte[] data) {
		Validation.notNull(data, "data-buffer must not be null");
	}

	private static void check(byte[] data, int off, int len) {
		check(data);
		Validation.notNegative(off, "offset must not be negative");
		Validation.notNegative(len, "length must not be negative");
		// (off + len) > data.length could overflow
		// but since all 3 parameters are in the range 0-Integer.MAX_VALUE the
		// calculation can be transformed
		if (len > (data.length - off)) {
			throw new IllegalArgumentException("not enough space in buffer");
		}
	}

	// shorten all those arraycopy calls
	// this also seems to boosts performance, maybe because src and dst are
	// strongly-typed and therefore better optimizable to a memcpy in the JIT'ed
	// code
	private static void ac(byte[] src, int srcPos, byte[] dst, int dstPos, int length) {
		System.arraycopy(src, srcPos, dst, dstPos, length);
	}
}
