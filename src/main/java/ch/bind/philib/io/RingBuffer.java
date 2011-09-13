package ch.bind.philib.io;

import ch.bind.philib.validation.SimpleValidation;

public class RingBuffer implements DoubleSidedBuffer {

    public static final int DEFAULT_CAPACITY = 4096;

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
        SimpleValidation.notNegative(capacity, "capacity");
        _init(capacity);
    }

    @Override
    public int available() {
        return ringSize;
    }

    @Override
    public void read(byte[] data) {
        SimpleValidation.notNull(data, "data-buffer");
        read(data, 0, data.length);
    }

    @Override
    public void read(byte[] data, int off, int len) {
        SimpleValidation.notNull(data, "data-buffer");
        SimpleValidation.notNegative(off, "offset");
        SimpleValidation.notNegative(len, "offset");
        _bufferSpaceCheck(data, off, len);

        if (len == 0) {
            return;
        }
        _readSizeCheck(len);
        _read(data, 0, len);
        _consumed(len);
    }

    @Override
    public void readBack(byte[] data) {
        SimpleValidation.notNull(data, "data-buffer");
        readBack(data, 0, data.length);
    }

    @Override
    public void readBack(byte[] data, int off, int len) {
        SimpleValidation.notNull(data, "data-buffer");
        SimpleValidation.notNegative(off, "offset");
        SimpleValidation.notNegative(len, "offset");
        _bufferSpaceCheck(data, off, len);

        _readSizeCheck(len);
        _readBack(data, 0, len);
        _consumedBack(len);
    }

    @Override
    public void write(byte[] data) {
        SimpleValidation.notNull(data, "data-buffer");
        write(data, 0, data.length);
    }

    @Override
    public void write(byte[] data, int off, int len) {
        SimpleValidation.notNull(data, "data-buffer");
        SimpleValidation.notNegative(off, "offset");
        SimpleValidation.notNegative(len, "offset");
        _bufferSpaceCheck(data, off, len);

        int newSize = ringSize + len;
        _ensureBufferSize(newSize);
        _write(data, off, len);
        ringSize = newSize;
    }

    @Override
    public void writeFront(byte[] data) {
        SimpleValidation.notNull(data, "data-buffer");
        writeFront(data, 0, data.length);
    }

    @Override
    public void writeFront(byte[] data, int off, int len) {
        SimpleValidation.notNull(data, "data-buffer");
        SimpleValidation.notNegative(off, "offset");
        SimpleValidation.notNegative(len, "offset");
        _bufferSpaceCheck(data, off, len);

        int newSize = ringSize + len;
        _ensureBufferSize(newSize);
        _writeFront(data, off, len);
        ringSize = newSize;
        ringOffset = _offsetMinus(len);
    }

    private void _init(int capacity) {
        this.ringCapacity = capacity;
        this.ringBuf = new byte[capacity];
    }

    private void _bufferSpaceCheck(byte[] data, int off, int len) {
        if (off + len > data.length) {
            throw new IllegalArgumentException("not enough space in buffer");
        }
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
        // read all data into the beginning of the new buffer
        _read(newBuf, 0, ringSize);
        this.ringBuf = newBuf;
        this.ringCapacity = newCap;
        this.ringOffset = 0;
    }

    private void _read(byte[] buf, int off, int len) {
        System.out.println("_read");
        int availToEnd = ringCapacity - ringOffset;
        if (availToEnd >= len) {
            // all data is available from one read
            ac(ringBuf, ringOffset, buf, off, len);
        } else {
            // read available space from the offset to the end of the buffer
            // then read the rest of the required data from the beginning
            int rem = ringSize - availToEnd;
            ac(ringBuf, ringOffset, buf, off, availToEnd);
            ac(ringBuf, 0, buf, off + availToEnd, rem);
        }
    }

    private void _readBack(byte[] buf, int off, int len) {
        System.out.println("_readBack");
        dumpVars();
        int firstReadOffset = _offsetPlus(ringSize - len);
        SimpleValidation.isTrue(firstReadOffset >= 0, "1");
        SimpleValidation.isTrue(firstReadOffset < ringCapacity, firstReadOffset + " < " + ringCapacity);

        int availToEnd = ringCapacity - firstReadOffset;
        int numReadOne = Math.min(availToEnd, len);
        SimpleValidation.isTrue(numReadOne >= 0, "3");
        SimpleValidation.isTrue(numReadOne <= len, "4");

        int numReadTwo = len - numReadOne;
        SimpleValidation.isTrue(numReadTwo >= 0, "5");
        ac(ringBuf, firstReadOffset, buf, off, numReadOne);

        if (numReadTwo > 0) {
            ac(ringBuf, 0, buf, off + numReadOne, numReadTwo);
        }
    }

    private void dumpVars() {
        System.out.printf("size=%d, offset=%d%n", ringSize, ringOffset);
        System.out.flush();
    }

    private void _write(byte[] data, int off, int len) {
        System.out.println("_write");
        int writePosOne = _offsetPlus(ringSize);
        int availBack = ringCapacity - writePosOne;
        int numWriteOne = Math.min(availBack, len);
        int numWriteTwo = len - numWriteOne;

        ac(data, off, ringBuf, writePosOne, numWriteOne);
        if (numWriteTwo > 0) {
            ac(data, off + numWriteOne, ringBuf, 0, numWriteTwo);
        }
    }

    private void _writeFront(byte[] data, int off, int len) {
        System.out.println("_writeFront");
        int writePosOne = _offsetMinus(len);
        int availBack = ringCapacity - writePosOne;
        int numWriteOne = Math.min(availBack, len);
        ac(data, off, ringBuf, writePosOne, numWriteOne);
        int numWriteTwo = len - numWriteOne;
        if (numWriteTwo > 0) {
            ac(data, off + numWriteOne, ringBuf, 0, numWriteTwo);
        }
    }

    private void _consumed(int len) {
        ringOffset = _offsetPlus(len);
        ringSize -= len;
    }

    private void _consumedBack(int len) {
        ringSize -= len;
    }

    private void _readSizeCheck(int size) {
        if (this.ringSize < size) {
            throw new IndexOutOfBoundsException();
        }
    }

    private int _offsetPlus(int shift) {
        // TODO: remove assertion
        SimpleValidation.isTrue(shift >= 0);
        int offset = ringOffset + shift;
        offset %= ringCapacity;
        return offset;
    }

    private int _offsetMinus(int shift) {
        // TODO: remove assertion
        SimpleValidation.isTrue(shift >= 0);
        int offset = ringOffset - shift;
        if (offset < 0) {
            offset += ringCapacity;
        }
        return offset;
    }

    static volatile boolean debug = true;

    private synchronized void ac(byte[] src, int srcPos, byte[] dst, int dstPos, int length) {
        if (debug) {
            System.out.printf("ac(byte[%d], %d, byte[%d], %d, %d)%n", src.length, srcPos, dst.length, dstPos, length);
            System.out.flush();
        }
        System.arraycopy(src, srcPos, dst, dstPos, length);
    }
}
