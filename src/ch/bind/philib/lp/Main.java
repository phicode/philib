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
 * 
 * @author Philipp Meinen
 */
public class Main {

	/**
	 * <pre>
	 * step 1:
	 *  -1    -1    40
	 * -40  -120  2400
	 *  -7   -12   312
	 * 100   250     0
	 * 
	 * step 2:
	 *   -1   -1    40
	 *   40  -80   800
	 *    7   -5    32
	 * -100  150  4000
	 * </pre>
	 */
	public static void main(String[] args) {
		LPMatrix matrix = new LPMatrix(2, 3);

		matrix.setSideCondition(0, new double[] { 1, 1 },
				SideConditionType.SMALLER_EQUAL, 40);
		matrix.setSideCondition(1, new double[] { 40, 120 },
				SideConditionType.SMALLER_EQUAL, 2400);
		matrix.setSideCondition(2, new double[] { 7, 12 },
				SideConditionType.SMALLER_EQUAL, 312);

		matrix.setTargetFunction(new double[] { 100, 250 });

		System.out.println(matrix);

		MatrixPoint pivot = matrix.findPivot();
		System.out.println("Pivot at: " + pivot);
		matrix.transform(pivot);
		System.out.println(matrix);

		pivot = matrix.findPivot();
		System.out.println("Pivot at: " + pivot);
		matrix.transform(pivot);
		System.out.println(matrix);

		pivot = matrix.findPivot();
		System.out.println("Pivot at: " + pivot);
	}
}
