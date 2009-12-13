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

import ch.bind.philib.ToStringUtil;

/**
 * 
 * @author Philipp Meinen
 */
public class LPMatrix {

	// Koeffizienten der Matrix, addressiert nach [x][y], bzw [spalte][zeile].
	// Die Anzahl Spalten entspricht der Anzahl Variablen (x) plus eins.
	// Die Anzahl Zeilen entspricht der Anzahl Nebenbedingunen plus eins.
	private final double[][] a;

	private final Variable[] headVars;
	private final Variable[] sideVars;

	private final int N;
	private final int M;

	public LPMatrix(int numVars, int numSideConds) {
		this.N = numVars;
		this.M = numSideConds;
		a = new double[N + 1][M + 1];

		this.headVars = new Variable[N];
		this.sideVars = new Variable[M];
		for (int k = 0; k < N; k++) {
			headVars[k] = new Variable(true, k);
		}
		for (int i = 0; i < M; i++) {
			sideVars[i] = new Variable(false, i);
		}
	}

	/**
	 * 
	 * @param pos
	 *            The position where the side-condition should be added.
	 * @param coeffs
	 * @param type
	 * @return The next position where a side-condition can be added.
	 */
	public int setSideCondition(final int pos, double[] coeffs,
			SideConditionType type) {
		if (pos < 0 || pos >= M)
			throw new IllegalArgumentException("position is out of range");
		if (coeffs == null || coeffs.length != (N + 1))
			throw new IllegalArgumentException("coeffs count invalid");
		if (type == null)
			throw new IllegalArgumentException("no type defined");

		if (type != SideConditionType.EQUAL) {
			boolean negateCoeff = type == SideConditionType.SMALLER_EQUAL;
			setSideCondition(pos, coeffs, negateCoeff);
			return pos + 1;
		} else {
			// we need one extra space because we are going to add two rows for
			// an equation
			if (pos >= (M - 1))
				throw new IllegalArgumentException("position is out of range");
			setSideCondition(pos, coeffs, true);
			setSideCondition(pos + 1, coeffs, false);
			return pos + 2;
		}
	}

	private void setSideCondition(final int pos, double[] coeffs,
			boolean negateCoeff) {
		double[] transformed = transformSideCondition(coeffs, negateCoeff);
		for (int i = 0; i <= N; i++) {
			a[i][pos] = transformed[i];
		}
	}

	// schlupfvariablen bilden:
	// c1 + c2 + ... + cn <= target
	// c1 + c2 + ... + cn + y = target
	// y = - c1 - c2 - ... - cn + target
	// TODO: only handles <= and => so far, the other case (=) must be added.
	// for <= : negateCoeff = true
	// a + b <= c
	// a + b + y = c
	// y = -a + -b + c
	// for >= : negateCoeff = false
	// a + b >= c
	// a + b - y = c
	// y = a + b - c
	private double[] transformSideCondition(double[] coeffs, boolean negateCoeff) {
		double multCoeff = negateCoeff ? -1 : 1;
		double multResult = negateCoeff ? 1 : -1;

		double[] transformed = new double[N + 1];
		for (int i = 0; i < N; i++) {
			transformed[i] = multCoeff * coeffs[i];
		}
		transformed[N] = multResult * coeffs[N];
		return transformed;
	}

	public void setTargetFunction(double[] coeffs) {
		if (coeffs == null || coeffs.length != (N + 1))
			throw new IllegalArgumentException("coeffs invalid");

		for (int i = 0; i <= N; i++) {
			a[i][M] = coeffs[i];
		}
	}

	public MatrixPoint findPivot() {
		// check for positive bq first
		for (int x = 0; x < N; x++) {
			if (a[x][M] > 0) {
				int y = findSmallestQuotient(x);
				if (y != -1)
					return new MatrixPoint(x, y);
			}
		}

		// now check for bq's with a value of zero
		for (int x = 0; x < N; x++) {
			if (a[x][M] == 0) {
				int y = findSmallestQuotient(x);
				if (y != -1)
					return new MatrixPoint(x, y);
			}
		}

		// no pivot found
		return null;
	}

	private int findSmallestQuotient(final int x) {
		double smallest = Double.MIN_VALUE;
		int row = -1;
		for (int y = 0; y < M; y++) {
			final double Ci = a[N][y];
			final double Aiq = a[x][y];
			if (Aiq < 0) {
				final double q = Math.abs(Ci / Aiq);
				if (row == -1 || q < smallest) {
					smallest = q;
					row = y;
				}
			}
		}
		return row;
	}

	/**
	 * <pre>
	 *     x1   x2
	 * y1  -2 + -2 = 3
	 * y2   4 + -5 = 6
	 *  z   7 + -8 = 9
	 * transform(1, 1) : (x1, y1)
	 * 
	 *  y1 = -2x1 + -2x2 + 3
	 * 2x1 = -y1 + -2x2 + 3
	 *  x1 = -1/2y1 + -x2 + 3/2
	 * 
	 * y2 = 4x1 + 5x2 + 6
	 * y2 = 4(-y1 + 2x2 + 3) + 5x2 + 6
	 * y2 = -4y1 + 8x2 + 12 + 5x2 + 6
	 * y2 = -4y1 + 13x2 + 8
	 * 
	 * z = 7x1 + 8x2 + 9
	 * z = 7(-y1 + 2x2 + 3) + 8x2 + 9
	 * z = -7y1 + 14x2 + 21 + 8x2 + 9
	 * z = -7y1 + 22x2 + 30
	 * 
	 * result:
	 *     y1   x2
	 * x1  -1/2 +  2 =  3
	 * y2  -4 + 13 =  8
	 *  z  -7 + 22 = 30
	 * </pre>
	 */
	public double transform(final MatrixPoint pivot) {
		final int x = pivot.getX();
		final int y = pivot.getY();
		if (x < 0 || x >= N)
			throw new IllegalArgumentException("x out of range");
		if (y < 0 || y >= M)
			throw new IllegalArgumentException("y out of range");

		transformPivotRow(x, y);
		swapVariableDefinitions(x, y);

		for (int row = 0; row <= M; row++) {
			if (row != y) {
				tranformRow(row, pivot);
			}
		}

		return a[N][M];
	}

	/**
	 * Transforms one of the non-pivot rows.
	 * 
	 * @param row
	 * @param pivot
	 */
	private void tranformRow(final int row, final MatrixPoint pivot) {
		// pivotRow: x1 = -y1 + 2x2 + 3
		// row: y2 = 4x1 + 5x2 + 6
		// row: y2 = 4(-y1 + 2x2 + 3) + 5x2 + 6
		// row: y2 = -4y1 + 8x2 + 12 + 5x2 + 6
		// row: y2 = -4y1 + 13x2 + 18
		// row -> -4 13 18
		// -4 = 0 + 4 * -1 <- pivot-column
		// 13 = 5 + 4 * 2
		// 18 = 6 + 4 * 3
		final int pivotX = pivot.getX();
		final int pivotY = pivot.getY();
		final double pivotColumnValue = a[pivotX][row];
		for (int x = 0; x <= N; x++) {
			if (x != pivotX) {
				a[x][row] += pivotColumnValue * a[x][pivotY];
			}
		}
		a[pivotX][row] *= a[pivotX][pivotY];
	}

	/**
	 * Swap the variable names
	 * 
	 * @param x
	 * @param y
	 */
	private void swapVariableDefinitions(int x, int y) {
		final Variable swapHead = headVars[x];
		headVars[x] = sideVars[y];
		sideVars[y] = swapHead;
	}

	/**
	 * the transformation from:<br/>
	 * y1 = -3x1 + 6x2 + 3<br/>
	 * to:<br/>
	 * 3x1 = -y1 + 6x2 + 3<br/>
	 * to: <br/>
	 * x1 = -1/3y1 + 2x2 + 1<br/>
	 * can easily be done by setting a minus 1 into the spot of the pivot
	 * element, followed by a division of all elements by the absolute value of
	 * the pivot.
	 * 
	 * 
	 * @param x
	 * @param y
	 */
	private void transformPivotRow(final int x, final int y) {
		final double absPivot = Math.abs(a[x][y]);
		a[x][y] = -1;
		for (int curx = 0; curx <= N; curx++) {
			a[curx][y] /= absPivot;
		}
	}

	private boolean isInfinite() {
		boolean allBnegative = true;
		for (int x = 0; x < N; x++) {
			if (a[x][M] >= 0)
				allBnegative = false;
		}
		if (allBnegative)
			return false;

		for (int x = 0; x < N; x++) {
			for (int y = 0; y < M; y++) {
				if (a[x][y] < 0)
					return false;
			}
		}
		return true;
	}

	public double getX(int searchX) {
		if (searchX < 0 || searchX >= N)
			throw new IllegalArgumentException("searchX is out of range");
		for (int i = 0; i < N; i++) {
			Variable var = headVars[i];
			if (var.isXVariable() && var.getNr() == searchX) {
				return a[i][M];
			}
		}
		for (int i = 0; i < M; i++) {
			Variable var = sideVars[i];
			if (var.isXVariable() && var.getNr() == searchX) {
				return a[N][i];
			}
		}
		throw new IllegalStateException("should never happen");
	}

	private static final class Variable {

		final boolean xVariable;
		final int nr;

		public Variable(boolean xVar, int nr) {
			this.xVariable = xVar;
			this.nr = nr;
		}

		@Override
		public String toString() {
			return (xVariable) ? "x" + nr : "y" + nr;
		}

		public boolean isXVariable() {
			return xVariable;
		}

		public int getNr() {
			return nr;
		}
	}

	public static final class MatrixPoint {
		final int x;
		final int y;

		private MatrixPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		@Override
		public String toString() {
			return "(" + x + " / " + y + ")";
		}
	}

	public String getSolution() {
		if (isInfinite()) {
			return "Infinite problem";
		}

		final String fmt = "x%d = %.9f\n";
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < N; x++) {
			double value = getX(x);
			sb.append(String.format(fmt, x + 1, value));
		}
		sb.append("Result: ");
		sb.append(a[N][M]);
		return sb.toString();
	}

	@Override
	public String toString() {
		final String fmt = "%.3f";
		final String[][] output = new String[N + 2][M + 2];
		for (int i = 0; i < N; i++) {
			output[i + 1][0] = headVars[i].toString();
		}
		output[0][0] = "";
		output[N + 1][0] = "";

		for (int i = 0; i < M; i++) {
			output[0][i + 1] = sideVars[i].toString();
		}
		output[0][M + 1] = "z";

		for (int y = 0; y <= M; y++) {
			for (int x = 0; x <= N; x++) {
				final double value = a[x][y];
				final String o = String.format(fmt, value);
				output[x + 1][y + 1] = o;
			}
		}

		return ToStringUtil.matrixOutput(output);
	}

}
