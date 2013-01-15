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

import static ch.bind.philib.lang.TableFormatter.NULL_TABLE;
import static ch.bind.philib.lang.TableFormatter.formatTable;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class TableFormatterTest {

	@Test
	public void nullTable() {
		assertEquals(formatTable(null), NULL_TABLE);

		assertEquals(formatTable(new Object[0][]), NULL_TABLE);

		Object[][] empty = {
				null, {} };
		assertEquals(formatTable(empty), NULL_TABLE);
	}

	@Test
	public void unevenTable() {
		Object[][] table = {
				{
						"a", 1 }, //
				{
						"b", "foo", 2, null }, //
				{
						null, null, new Evil() }, // empty
				{}, // also empty
				null, // empty again
				{
						"c", "baz5", null, "qed" }, //
		};
		String exp = "+---+------+---+-----+\n" + //
				"| a | 1    |   |     |\n" + //
				"+---+------+---+-----+\n" + //
				"| b | foo  | 2 |     |\n" + //
				"|   |      |   |     |\n" + //
				"|   |      |   |     |\n" + //
				"|   |      |   |     |\n" + //
				"| c | baz5 |   | qed |\n" + //
				"+---+------+---+-----+";
		assertEquals(formatTable(table), exp);
	}

	@Test
	public void emptyColumn() {
		Object[][] table = {
				{
						"a", null, 1 }, //
				{
						"b", null, 2 }, //
				{
						"c", null, 3 }, //
		};
		String exp = "+---+-+---+\n" + //
				"| a | | 1 |\n" + //
				"+---+-+---+\n" + //
				"| b | | 2 |\n" + //
				"| c | | 3 |\n" + //
				"+---+-+---+";
		assertEquals(formatTable(table), exp);
	}

	@Test
	public void changingLength() {
		Object[][] table = {
				{
						new LongerStr(), new ShorterStr() }, //
				{
						new ShorterStr(), new LongerStr() }, //
		};
		String exp = "+------+------+\n" + //
				"| abcd | xyz  |\n" + //
				"+------+------+\n" + //
				"| xyz  | abcd |\n" + //
				"+------+------+";
		assertEquals(formatTable(table), exp);
	}

	private static class Evil {

		@Override
		public String toString() {
			return null;
		}
	}

	private static class LongerStr {

		private int toStrCount;

		@Override
		public String toString() {
			toStrCount++;
			if (toStrCount == 1) {
				return "abc";
			} else if (toStrCount == 2) {
				return "abcdef";
			} else {
				throw new AssertionError();
			}
		}
	}

	private static class ShorterStr {

		private int toStrCount;

		@Override
		public String toString() {
			toStrCount++;
			if (toStrCount == 1) {
				return "wxyz";
			} else if (toStrCount == 2) {
				return "xyz";
			} else {
				throw new AssertionError();
			}
		}
	}
}
