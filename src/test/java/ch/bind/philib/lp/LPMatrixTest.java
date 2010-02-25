/*
 * Copyright (c) 2009 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.lp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ch.bind.philib.lp.LPMatrix.MatrixPoint;

public class LPMatrixTest {

    @Test
    public void matrix() {
        LPMatrix matrix = new LPMatrix(2, 3);
        matrix.setSideCondition(0, new double[] { 1, 1, 40 }, SideConditionType.SMALLER_EQUAL);
        matrix.setSideCondition(1, new double[] { 40, 120, 2400 }, SideConditionType.SMALLER_EQUAL);
        matrix.setSideCondition(2, new double[] { 7, 12, 312 }, SideConditionType.SMALLER_EQUAL);
        matrix.setTargetFunction(new double[] { 100, 250, 0 });

        System.out.println(matrix);

        // STEP 1
        MatrixPoint pivot = matrix.findPivot();
        assertNotNull(pivot);
        assertEquals(0, pivot.getX());
        assertEquals(0, pivot.getY());

        double result = matrix.transform(pivot);
        assertEquals(4000.0, result, 0.0);
        System.out.println(matrix);

        // STEP 2
        pivot = matrix.findPivot();
        assertNotNull(pivot);
        assertEquals(1, pivot.getX());
        assertEquals(2, pivot.getY());

        result = matrix.transform(pivot);
        assertEquals(4960.0, result, 0.0);
        System.out.println(matrix);

        // STEP 3
        pivot = matrix.findPivot();
        assertNotNull(pivot);
        assertEquals(0, pivot.getX());
        assertEquals(1, pivot.getY());

        result = matrix.transform(pivot);
        assertEquals(5400.0, result, 0.0);
        System.out.println(matrix);

        // NO STEP 4
        pivot = matrix.findPivot();
        assertNull(pivot);

        // CHECK RESULTS
        assertEquals(24.0, matrix.getX(0), 0.0);
        assertEquals(12.0, matrix.getX(1), 0.0);
    }
}
