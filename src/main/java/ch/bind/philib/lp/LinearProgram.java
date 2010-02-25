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

/**
 * Describes a linear program.
 * 
 * @author Philipp Meinen
 */
public final class LinearProgram {

    private final int numVars;
    private final int numSideConditions;
    private ProblemType problemType;
    private final double[] targetFunction;
    private final boolean[] nonNegativity;
    private final double[][] sideConditions;
    private final SideConditionType[] sideConditionTypes;

    public LinearProgram(int numVars, int numSideConditions) {
        this.numVars = numVars;
        this.numSideConditions = numSideConditions;

        targetFunction = new double[numVars + 1];
        nonNegativity = new boolean[numVars];
        sideConditions = new double[numSideConditions][numVars + 1];
        sideConditionTypes = new SideConditionType[numSideConditions];
    }

    public int getNumVars() {
        return numVars;
    }

    public int getNumSideConditions() {
        return numSideConditions;
    }

    public ProblemType getProblemType() {
        return problemType;
    }

    public void setProblemType(ProblemType problemType) {
        this.problemType = problemType;
    }

    public double[][] getSideConditions() {
        return sideConditions;
    }

    public void setSideCondition(int num, double[] coeffs) {
        if (num < 0 || num >= numSideConditions)
            throw new IllegalArgumentException("too many side-conditions");
        if (coeffs == null || coeffs.length != (numVars + 1))
            throw new IllegalArgumentException("invalid number of side-condition coefficients");
        System.arraycopy(coeffs, 0, sideConditions[num], 0, numVars + 1);
    }

    public double[] getTargetFunction() {
        return targetFunction;
    }

    public void setTargetFunction(double[] targetFunction) {
        final int N = this.targetFunction.length;
        if (targetFunction == null || targetFunction.length != N)
            throw new IllegalArgumentException("invalid target-function length");
        System.arraycopy(targetFunction, 0, this.targetFunction, 0, N);
    }

    public SideConditionType[] getSideConditionTypes() {
        return sideConditionTypes;
    }

    public void setSideConditionType(int num, SideConditionType type) {
        if (num < 0 || num >= numSideConditions)
            throw new IllegalArgumentException("too many side-conditions");
        if (type == null)
            throw new IllegalArgumentException("no side-condition type defined");
        this.sideConditionTypes[num] = type;
    }

    public boolean[] getNonNegativity() {
        return nonNegativity;
    }

    public void setNonNegativity(int num, boolean value) {
        if (num < 0 || num >= numVars)
            throw new IllegalArgumentException("illegal index for a non-negativity flag");
        nonNegativity[num] = value;
    }
}
