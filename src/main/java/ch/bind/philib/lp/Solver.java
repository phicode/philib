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

import ch.bind.philib.lp.LPMatrix.MatrixPoint;

/**
 * Solves a linear program.
 * 
 * @author Philipp Meinen
 */
public final class Solver {

    private final LinearProgram linearProgram;

    public Solver(LinearProgram linearProgram) {
        this.linearProgram = linearProgram;
    }

    public String solve() {
        final LPMatrix matrix = buildMatrix();
        double lastResult;
        double result;

        MatrixPoint pivot = matrix.findPivot();
        if (pivot != null) {
            result = matrix.transform(pivot);

            do {
                lastResult = result;
                pivot = matrix.findPivot();
                if (pivot == null)
                    break;
                result = matrix.transform(pivot);
            } while (lastResult != result);
        }
        checkNonNegativity(matrix);

        // System.out.println(matrix);
        return matrix.getSolution();
    }

    private void checkNonNegativity(final LPMatrix matrix) {
        boolean[] nonNegativity = linearProgram.getNonNegativity();
        for (int x = 0; x < linearProgram.getNumVars(); x++) {
            if (matrix.getX(x) < 0 && nonNegativity[x] == true) {
                throw new IllegalStateException("x" + (x + 1) + " became negative but is not allowed to do so");
            }
        }
    }

    /**
     * Transforms a linear-program into a linear-program matrix.
     * 
     * @return The resulting linear-program matrix.
     */
    private LPMatrix buildMatrix() {
        final LinearProgram lp = linearProgram;
        final int N = lp.getNumVars();
        // final int NActual = getActualNumVars(lp);
        final int M = lp.getNumSideConditions();
        final int MActual = getActualNumSideConditions(lp);
        LPMatrix m = new LPMatrix(N, MActual);

        // boolean[] nonNeg = lp.getNonNegativity();

        double[][] scs = lp.getSideConditions();
        SideConditionType[] sct = lp.getSideConditionTypes();
        int pos = 0;
        for (int i = 0; i < M; i++) {
            pos = m.setSideCondition(pos, scs[i], sct[i]);
        }

        ProblemType type = lp.getProblemType();
        if (type == ProblemType.MAXIMIZE) {
            m.setTargetFunction(lp.getTargetFunction());
        } else {
            double[] maxProblem = minToMaxProblem(lp.getTargetFunction());
            m.setTargetFunction(maxProblem);
        }

        return m;
    }

    // /**
    // * Variables which are allowed to become non-negative are transformed into
    // * two variables which are not allowed to become negative.<br/>
    // * a + b <= 10<br>
    // * a >= 0<br/>
    // * b <= 0<br/>
    // * transform b to <code>(z1 - z2)</code> so that:<br/>
    // * a + z1 - z2 <= 10
    // *
    // * @param lp
    // * @return
    // */
    // private int getActualNumVars(LinearProgram lp) {
    // int num = 0;
    // for (boolean nonNeg : lp.getNonNegativity()) {
    // num += (nonNeg ? 1 : 2);
    // }
    // return num;
    // }

    /**
     * The actual number of side-conditions can be higher then the number which
     * is stored in the linear program description. The reason for this is, that
     * a side-condition which is formulated as an equation instead of an
     * inequation is transformed into two inequations.<br/>
     * <code>a + b <= c</code> becomes: <code>a + b + y = c</code> -&gt; 1
     * equation<br/>
     * <code>a + b >= c</code> becomes: <code>a + b - y = c</code> -&gt; 1
     * equation<br/>
     * <code>a + b <= c</code> becomes: <br/>
     * <code>a + b + y = c</code> and<br/>
     * <code>a + b - y = c</code><br />
     * -&gt; 2 equations.
     * 
     * @param lp
     * @return
     */
    private int getActualNumSideConditions(LinearProgram lp) {
        int num = 0;
        for (SideConditionType type : lp.getSideConditionTypes()) {
            if (type == SideConditionType.EQUAL) {
                num += 2;
            } else {
                num++;
            }
        }
        return num;
    }

    private double[] minToMaxProblem(double[] targetFunction) {
        // minimize: a + b
        // -> maximize -(a + b)
        final int N = targetFunction.length;
        for (int i = 0; i < N; i++) {
            targetFunction[i] = -targetFunction[i];
        }
        return targetFunction;
    }

}
