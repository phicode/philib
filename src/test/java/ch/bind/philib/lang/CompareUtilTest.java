package ch.bind.philib.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.bind.philib.lang.CompareUtil;

public class CompareUtilTest {

    @Test
    public void equalityObjObj() {
        Object a = "a";
        Object b = "b";

        boolean eq = CompareUtil.equals(a, b);
        assertFalse(eq);

        a = "b";
        eq = CompareUtil.equals(a, b);
        assertTrue(eq);

        b = "a";
        eq = CompareUtil.equals(a, b);
        assertFalse(eq);
    }

    @Test
    public void equalityNullNull() {
        Object a = null;
        Object b = null;

        boolean eq = CompareUtil.equals(a, b);
        assertTrue(eq);
    }

    @Test
    public void equalityObjNull() {
        Object a = "a";
        Object b = null;

        boolean eq = CompareUtil.equals(a, b);
        assertFalse(eq);
    }

    @Test
    public void equalityNullObj() {
        Object a = null;
        Object b = "b";

        boolean eq = CompareUtil.equals(a, b);
        assertFalse(eq);
    }

    @Test
    public void compareStringString() {
        String a = "a";
        String b = "b";

        int cmp = CompareUtil.compare(a, b);
        assertEquals(-1, cmp);

        a = "b";
        cmp = CompareUtil.compare(a, b);
        assertEquals(0, cmp);

        b = "a";
        cmp = CompareUtil.compare(a, b);
        assertEquals(1, cmp);
    }

    @Test
    public void compareStringNull() {
        String a = "a";
        String b = null;

        int cmp = CompareUtil.compare(a, b);
        assertEquals(1, cmp); // a > b
    }

    @Test
    public void compareNullString() {
        String a = null;
        String b = "b";

        int cmp = CompareUtil.compare(a, b);
        assertEquals(-1, cmp); // a < b
    }

    @Test
    public void compareNullNull() {
        String a = null;
        String b = null;

        int cmp = CompareUtil.compare(a, b);
        assertEquals(0, cmp); // a == b
    }
}
