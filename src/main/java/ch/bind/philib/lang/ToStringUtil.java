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

package ch.bind.philib.lang;

/**
 * 
 * @author Philipp Meinen
 */
public final class ToStringUtil {

	private ToStringUtil() {
	}

	/**
	 * Pretty-prints a matrix.
	 * 
	 * @param matrix The matrix which must be printed in a friendly way.
	 * @return The result of the matrix pretty-printing.
	 */
	public static String matrixOutput(String[][] matrix) {
		checkMatrix(matrix);
		StringBuilder sb = new StringBuilder();
		final int N = matrix.length;
		if (N == 0)
			return "";
		final int M = matrix[0].length;

		int max = 0;
		for (int x = 0; x < N; x++) {
			for (int y = 0; y < M; y++) {
				final String str = matrix[x][y];
				if (str != null) {
					int len = matrix[x][y].length();
					if (len > max) {
						max = len;
					}
				}
			}
		}
		// N * <maximale länge> + (N-1) mal " | "
		int linelen = N * max + (N - 1) * 3;
		char[] linepad = new char[linelen + 2];
		for (int x = 1; x <= linelen; x++)
			linepad[x] = '-';
		linepad[0] = linepad[linelen + 1] = '\n';

		for (int y = 0; y < M; y++) {
			for (int x = 0; x < N; x++) {
				if (x != 0)
					sb.append(' ');
				String val = matrix[x][y];
				if (val == null)
					val = "";
				int len = val.length();
				int pad = max - len;
				for (int p = 0; p < pad; p++)
					sb.append(' ');

				sb.append(val);

				if (x < (N - 1))
					sb.append(" |");
			}
			sb.append(linepad);
		}

		return sb.toString();
	}

	private static void checkMatrix(String[][] matrix) {
		if (matrix == null) {
			throw new IllegalArgumentException("matrix == null");
		}
		final int N = matrix.length;
		if (N > 0) {
			final String[] first = matrix[0];
			if (first == null)
				throw new IllegalArgumentException("matrix[0] == null");
			final int M = first.length;
			for (int i = 1; i < N; i++) {
				final String[] cur = matrix[i];
				if (cur == null)
					throw new IllegalArgumentException("matrix[" + i + "] == null");
				if (cur.length != M)
					throw new IllegalArgumentException("matrix[" + i + "].length != matrix[0].length");
			}
		}
	}

	public static void firstObj(StringBuilder sb, String name, Object obj) {
		sb.append(name);
		sb.append('=');
		sb.append(obj);
	}

	public static void addObj(StringBuilder sb, String name, Object obj) {
		sb.append(", ");
		firstObj(sb, name, obj);
	}

	public static void addObj(StringBuilder sb, Object obj) {
		sb.append(", ");
		sb.append(obj);
	}

	public static void firstInt(StringBuilder sb, String name, int v) {
		sb.append(name);
		sb.append('=');
		sb.append(v);
	}

	public static void addInt(StringBuilder sb, String name, int v) {
		sb.append(", ");
		firstInt(sb, name, v);
	}

	public static void firstLong(StringBuilder sb, String name, long v) {
		sb.append(name);
		sb.append('=');
		sb.append(v);
	}

	public static void addLong(StringBuilder sb, String name, long v) {
		sb.append(", ");
		firstLong(sb, name, v);
	}

	public static void firstStr(StringBuilder sb, String name, String v) {
		sb.append(name);
		sb.append('=');
		sb.append(v);
	}

	public static void addStr(StringBuilder sb, String name, String v) {
		sb.append(", ");
		firstStr(sb, name, v);
	}
}
