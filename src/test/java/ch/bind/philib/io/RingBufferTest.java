package ch.bind.philib.io;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class RingBufferTest {

    private Random rand = new Random();

    private static final int TEST_BUF_SIZE = 1 * 1024 * 1024;

    private static final int RANDOM_PERF_TEST_SIZE = 16 * 1024 * 1024;

    @Test
    public void frontAndBack() {
        RingBuffer.debug = false;
        LinkedList<byte[]> bufExp = new LinkedList<byte[]>();
        RingBuffer buf = new RingBuffer();
        assertEquals(0, buf.available());
        final int chunkSize = 16;
        int chunkIdx = 0;
        int size = 0;
        while (size < TEST_BUF_SIZE) {
            byte[] d = genData(chunkSize);
            if (chunkIdx % 2 == 0) {
                buf.write(d);
                bufExp.addLast(d);
            } else {
                buf.writeFront(d);
                bufExp.addFirst(d);
            }
            size += chunkSize;
            assertEquals(size, buf.available());
            chunkIdx++;
        }
        verifyBuf(bufExp, buf);
    }

    @Test
    public void randomAccess() {
        RingBuffer.debug = true;
        LinkedList<Byte> bufExp = new LinkedList<Byte>();
        RingBuffer buf = new RingBuffer();
        int size = 0;
        int performed = 0;
        while (performed < RANDOM_PERF_TEST_SIZE) {
            int num = rand.nextInt(32) + 1;
            byte[] b = new byte[num];
            boolean doRead = buf.available() >= num ? rand.nextBoolean() : false;
            boolean doFront = rand.nextBoolean();
            if (doRead) {
                if (doFront) {
                    buf.read(b);
                    verifyRead(b, bufExp);
                } else {
                    buf.readBack(b);
                    verifyReadBack(b, bufExp);
                }
                size -= num;
            } else {
                rand.nextBytes(b);
                if (doFront) {
                    buf.writeFront(b);
                    prepend(b, bufExp);
                } else {
                    buf.write(b);
                    append(b, bufExp);
                }
                size += num;
            }
            assertEquals(size, buf.available());
            assertEquals(size, bufExp.size());
            performed += num;
        }
    }

    private void verifyReadBack(byte[] bs, LinkedList<Byte> bufExp) {
        for (int i = bs.length - 1; i >= 0; i--) {
            byte b = bs[i];
            byte e = bufExp.removeLast();
            assertEquals(e, b);
        }
    }

    private void verifyRead(byte[] bs, LinkedList<Byte> bufExp) {
        for (byte b : bs) {
            byte e = bufExp.removeFirst();
            assertEquals(e, b);
        }
    }

    private void prepend(byte[] bs, LinkedList<Byte> bufExp) {
        for (int i = bs.length - 1; i >= 0; i--) {
            bufExp.addFirst(bs[i]);
        }
    }

    private void append(byte[] bs, LinkedList<Byte> bufExp) {
        for (byte b : bs) {
            bufExp.add(b);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void notNullRead() {
        RingBuffer buf = new RingBuffer();
        buf.read(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notNullReadOffLen() {
        RingBuffer buf = new RingBuffer();
        buf.read(null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notNegativeOffsetRead() {
        RingBuffer buf = new RingBuffer();
        byte[] b = new byte[16];
        buf.write(b);
        buf.read(b, -1, 16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notNegativeLengthRead() {
        RingBuffer buf = new RingBuffer();
        byte[] b = new byte[16];
        buf.write(b);
        buf.read(b, 0, -1);
    }

    private void verifyBuf(List<byte[]> expected, RingBuffer buf) {
        byte[] a = null;
        int expSize = buf.available();
        for (byte[] e : expected) {
            if (a == null || a.length != e.length) {
                a = new byte[e.length];
            }
            assertEquals(expSize, buf.available());
            buf.read(a);
            expSize -= a.length;
            assertTrue(Arrays.equals(e, a));
            assertEquals(expSize, buf.available());
        }
        assertEquals(0, buf.available());
    }

    private byte[] genData(int num) {
        byte[] d = new byte[num];
        rand.nextBytes(d);
        return d;
    }
}
