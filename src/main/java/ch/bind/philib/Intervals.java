/*
 * Copyright (c) 2009-2010 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib;

public final class Intervals {

    private Intervals() {
    }

    private static final double[] COEFFS = { 1.0, 2.5, 5.0 };

    public static int chooseInterval(int maxValue, int maxSegments) {
        // try to bring maxSegments or less lines on to the chart
        int interval = 1;
        int segments = maxValue;

        // use these intervals:
        // 1, 2, 5,
        // 10, 25, 50
        // 100, 250, 500
        // ... and so on
        int intervalNum = 0;
        while (segments > maxSegments) {
            intervalNum++;

            // 0 for 1, 2, 5
            // 1 for 10, 25, 50
            // 2 for 100, 250, 500
            int power = (intervalNum / 3);
            double multiply = Math.pow(10, power);
            int num = intervalNum % 3;
            double coeff = COEFFS[num];

            // for num=0: 1, 10, 100, 100, ...
            // for num=1: 2, 25, 250, 2500, ...
            // for num=2: 5, 50, 500, 5000, ...
            interval = (int) (coeff * multiply);

            segments = maxValue / interval;
            if (segments * interval < maxValue)
                segments++;
        }

        return interval;
    }
}
