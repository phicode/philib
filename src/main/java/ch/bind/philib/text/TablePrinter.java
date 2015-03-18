/*
 * Copyright (c) 2015 Philipp Meinen <philipp@bind.ch> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software. THE
 * SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.bind.philib.text;

import java.io.IOException;
import java.util.Collection;

import ch.bind.philib.validation.Validation;

public final class TablePrinter {

	public interface FieldAccessor<T> {

		int numFields();

		String getField(T object, int index);
	}

	public static final class ArrayFieldAccessor implements FieldAccessor<Object> {

		private final Object[] fields;

		public ArrayFieldAccessor(Object[] fields) {
			this.fields = Validation.notNullOrEmpty(fields);
		}

		@Override
		public int numFields() {
			return fields.length;
		}

		@Override
		public String getField(Object object, int index) {
			Validation.isTrue(fields == object);
			return fields[index].toString();
		}
	}

	// TODO: option for borderless printing
	// TODO: unit test
	// TODO: title row

	/**
	 * Pretty-prints a table in row major order.
	 * @param table The table which must be printed in a friendly way. The first row is considered the 'title' row.
	 * @return The result of the table pretty-printing.
	 * @throws IOException
	 */
	public static <T> void print(Appendable appendable, Collection<? extends T> collection, FieldAccessor<T> accessor) throws IOException {
		int rows = collection.size();
		int nf = accessor.numFields();
		String[][] table = new String[rows][nf];
		int[] columnLens = new int[nf];
		int irow = 0;
		for (T row : collection) {
			for (int f = 0; f < nf; f++) {
				String s = accessor.getField(row, f);
				table[irow][f] = s;
				columnLens[f] = Math.max(columnLens[f], s.length());
			}
			irow++;
		}
		formatTable(appendable, table, columnLens);
	}

	public static void print(Appendable appendable, String[][] table) throws IOException {
		Validation.notNullOrEmpty(table);
		Validation.equalLengths(table);
		formatTable(appendable, table, calcColumnLens(table));
	}

	public static void formatTable(Appendable appendable, String[][] table, int[] columnLens) throws IOException {
		Validation.notNull(appendable);
		Validation.notNullOrEmpty(table);
		Validation.equalLengths(table);

		for (String[] row : table) {
			for (int i = 0; i < row.length; i++) {
				String s = row[i];
				appendable.append(s);
				for (int x = s.length(); x < columnLens[i]; x++) {
					appendable.append(' ');
				}
				appendable.append(' ');
			}
			appendable.append('\n');
		}
	}

	private static int[] calcColumnLens(String[][] table) {
		int l = table[0].length;
		int[] columnLens = new int[l];
		for (String[] row : table) {
			for (int i = 0; i < l; i++) {
				String v = row[i];
				if (v == null) {
					continue;
				}
				columnLens[i] = Math.max(columnLens[i], v.length());
			}
		}
		return columnLens;
	}
}
