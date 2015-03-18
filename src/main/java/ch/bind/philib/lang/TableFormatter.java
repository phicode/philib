/*
 * Copyright (c) 2006-2011 Philipp Meinen <philipp@bind.ch>
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

import java.util.Arrays;
import java.util.Collection;

public class TableFormatter {

	static final String NULL_TABLE = "+-+\n| |\n+-+";

	public interface FieldAccessor {
		Object getField(int index);
	}

	public static <T extends FieldAccessor> void formatTable(Appendable appendable, Collection<T> collection, int numFields, FieldAccessor accessor) {
		//TODO: unit test
		//TODO: cache length while 
		//TODO: title row
		int rows = collection.size();
		Object[][] table = new Object[rows][numFields];
		for (int row = 0; row < rows; row++) {
			for (int f = 0; f < numFields; f++) {
				table[row][f] = accessor.getField(f);
			}
		}
		formatTable(appendable, table);
	}

	//TODO: option for borderless printing
	public static void formatTable(Appendable appendable, Object[][] table) {
		// TODO
	}

	/**
	 * Pretty-prints a table in row major order.
	 *
	 * @param table The table which must be printed in a friendly way. The first row is considered
	 *            the 'title' row.
	 * @return The result of the table pretty-printing.
	 */
	// TODO: String[][]
	// TODO: remove in favor of the api above
	public static String formatTable(Object[][] table) {
		if (table == null || table.length == 0) {
			return NULL_TABLE;
		}
		int[] colWidths = calcTableColumnWidths(table);
		if (colWidths == null) {
			return NULL_TABLE;
		}
		int prealloc = colWidths.length * table.length * 32;
		StringBuilder sb = new StringBuilder(prealloc);

		printSeparatorRow(sb, colWidths, true);
		printRow(sb, colWidths, table[0]);
		printSeparatorRow(sb, colWidths, true);
		for (int i = 1; i < table.length; i++) {
			Object[] columns = table[i];
			if (columns == null || columns.length == 0) {
				printEmptyRow(sb, colWidths);
			} else {
				printRow(sb, colWidths, columns);
			}
		}
		printSeparatorRow(sb, colWidths, false);

		return sb.toString();
	}

	private static int[] calcTableColumnWidths(Object[][] table) {
		int[] colWidths = null;
		for (Object[] row : table) {
			if (row == null || row.length == 0) {
				continue;
			}
			int l = row.length;
			if (colWidths == null) {
				colWidths = new int[l];
			} else if (l > colWidths.length) {
				colWidths = Arrays.copyOf(colWidths, l);
			}
			for (int i = 0; i < l; i++) {
				Object cell = row[i];
				if (cell != null) {
					String s = cell.toString();
					if (s != null) {
						colWidths[i] = Math.max(colWidths[i], s.trim().length());
					}
				}
			}
		}
		return colWidths;
	}

	private static void printSeparatorRow(StringBuilder sb, int[] colWidths, boolean lf) {
		printRow(sb, colWidths, '+', '-', lf);
	}

	private static void printEmptyRow(StringBuilder sb, int[] colWidths) {
		printRow(sb, colWidths, '|', ' ', true);
	}

	private static void printRow(StringBuilder sb, int[] colWidths, char a, char b, boolean lf) {
		sb.append(a);
		for (int colWidth : colWidths) {
			if (colWidth == 0) {
				sb.append(b);
			} else {
				// 1 space to both sides
				for (int i = 0; i < (colWidth + 2); i++) {
					sb.append(b);
				}
			}
			sb.append(a);
		}
		if (lf) {
			sb.append('\n');
		}
	}

	private static void printRow(StringBuilder sb, int[] colWidths, Object[] columns) {
		sb.append('|');
		for (int i = 0; i < colWidths.length; i++) {
			int colWidth = colWidths[i];
			if (colWidth == 0) {
				sb.append(' ');
			} else {
				sb.append(' ');
				int used = 0;
				Object v = columns.length > i ? columns[i] : null;
				if (v != null) {
					String s = v.toString();
					if (s != null) {
						s = s.trim();
						if (s.length() > colWidth) {
							s = s.substring(0, colWidth);
						}
						sb.append(s);
						used = s.length();
					}
				}
				for (int fill = used; fill < colWidth; fill++) {
					sb.append(' ');
				}
				sb.append(' ');
			}
			sb.append('|');
		}
		sb.append('\n');
	}
}
