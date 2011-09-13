package ch.bind.philib.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import ch.bind.philib.lang.ArrayUtils;

public class ArrayUtilsTest {

    @Test(expected = NullPointerException.class)
    public void sourceNull() {
        Object[] arr = new Object[1];

        ArrayUtils.pickRandom(null, arr);
    }

    @Test(expected = NullPointerException.class)
    public void destinationNull() {
        Object[] arr = new Object[1];
        ArrayUtils.pickRandom(arr, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sourceSmallerThenDestination() {
        Object[] src = new Object[1];
        Object[] dst = new Object[2];
        ArrayUtils.pickRandom(src, dst);
    }

    @Test
    public void equalSize() {
        final int N = 4096;
        Integer[] src = new Integer[N];
        Integer[] dst = new Integer[N];
        boolean[] found = new boolean[N];
        for (int i = 0; i < N; i++) {
            src[i] = i;
        }
        ArrayUtils.pickRandom(src, dst);
        for (int i = 0; i < N; i++) {
            int v = dst[i].intValue();
            assertTrue(v >= 0);
            assertTrue(v < N);
            assertFalse(found[v]);
            found[v] = true;
        }
    }

    @Test
    public void concatNullNull() {
        byte[] r = ArrayUtils.concat(null, null);
        assertNotNull(r);
        assertEquals(0, r.length);
    }

    @Test
    public void concatNullEmpty() {
        byte[] r = ArrayUtils.concat(null, ArrayUtils.EMPTY_BYTE_ARRAY);
        assertNotNull(r);
        assertEquals(0, r.length);
    }

    @Test
    public void concatEmptyNull() {
        byte[] r = ArrayUtils.concat(ArrayUtils.EMPTY_BYTE_ARRAY, null);
        assertNotNull(r);
        assertEquals(0, r.length);
    }

    @Test
    public void concatEmptyEmpty() {
        byte[] r = ArrayUtils.concat(ArrayUtils.EMPTY_BYTE_ARRAY, ArrayUtils.EMPTY_BYTE_ARRAY);
        assertNotNull(r);
        assertEquals(0, r.length);
    }

    @Test
    public void concatNormalNull() {
        byte[] a = "123".getBytes();
        byte[] b = null;
        byte[] c = ArrayUtils.concat(a, b);
        assertNotNull(c);
        assertEquals(3, c.length);
        assertTrue(Arrays.equals(a, c));
    }

    @Test
    public void concatNullNormal() {
        byte[] a = null;
        byte[] b = "123".getBytes();
        byte[] c = ArrayUtils.concat(a, b);
        assertNotNull(c);
        assertEquals(3, c.length);
        assertTrue(Arrays.equals(b, c));
    }

    @Test
    public void concatNormalNormal() {
        byte[] a = "123".getBytes();
        byte[] b = "abc".getBytes();
        byte[] c = ArrayUtils.concat(a, b);
        byte[] ce = "123abc".getBytes();
        assertNotNull(c);
        assertEquals(6, c.length);
        assertTrue(Arrays.equals(ce, c));
    }
}
