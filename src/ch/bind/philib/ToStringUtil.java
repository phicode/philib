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

package ch.bind.philib;

/**
 * 
 * @author Philipp Meinen
 */
public final class ToStringUtil {

	private ToStringUtil() {
	}

	public static String matrixOutput(String[][] matrix) {
		StringBuilder sb = new StringBuilder();
		final int N = matrix.length;
		final int M = matrix[0].length;

		int max = 0;
		for (int x = 0; x < N; x++) {
			for (int y = 0; y < M; y++) {
				int len = matrix[x][y].length();
				if (len > max) {
					max = len;
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
}