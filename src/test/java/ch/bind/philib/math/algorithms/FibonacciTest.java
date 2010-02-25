package ch.bind.philib.math.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FibonacciTest {

    private Fibonacci fib;

    @Before
    public void setup() {
        this.fib = new Fibonacci();
    }

    @After
    public void tearDown() {
        this.fib = null;
    }

    @Test
    public void nextFib() {
        assertEquals(1, fib.nextFib());
        assertEquals(2, fib.nextFib());
        assertEquals(3, fib.nextFib());
        assertEquals(5, fib.nextFib());
        assertEquals(8, fib.nextFib());
        assertEquals(13, fib.nextFib());
        assertEquals(21, fib.nextFib());
        assertEquals(34, fib.nextFib());
        assertEquals(55, fib.nextFib());
        assertEquals(89, fib.nextFib());
        assertEquals(144, fib.nextFib());
        assertEquals(233, fib.nextFib());
        assertEquals(377, fib.nextFib());
        assertEquals(610, fib.nextFib());
        assertEquals(987, fib.nextFib());
        assertEquals(1597, fib.nextFib());
    }

    @Test
    public void calcFib() {
        assertEquals(1, Fibonacci.calcFib(1));
        assertEquals(2, Fibonacci.calcFib(2));
        assertEquals(3, Fibonacci.calcFib(3));
        assertEquals(5, Fibonacci.calcFib(4));
        assertEquals(8, Fibonacci.calcFib(5));
        assertEquals(13, Fibonacci.calcFib(6));
        assertEquals(21, Fibonacci.calcFib(7));
        assertEquals(34, Fibonacci.calcFib(8));
        assertEquals(55, Fibonacci.calcFib(9));
        assertEquals(89, Fibonacci.calcFib(10));
        assertEquals(144, Fibonacci.calcFib(11));
        assertEquals(233, Fibonacci.calcFib(12));
        assertEquals(377, Fibonacci.calcFib(13));
        assertEquals(610, Fibonacci.calcFib(14));
        assertEquals(987, Fibonacci.calcFib(15));
        assertEquals(1597, Fibonacci.calcFib(16));
    }
}
