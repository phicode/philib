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

import static ch.bind.philib.lang.StringUtil.addInt;
import static ch.bind.philib.lang.StringUtil.addLong;
import static ch.bind.philib.lang.StringUtil.addObj;
import static ch.bind.philib.lang.StringUtil.end;
import static ch.bind.philib.lang.StringUtil.extractBack;
import static ch.bind.philib.lang.StringUtil.firstInt;
import static ch.bind.philib.lang.StringUtil.firstLong;
import static ch.bind.philib.lang.StringUtil.firstObj;
import static ch.bind.philib.lang.StringUtil.start;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class StringUtilTest {

	@Test
	public void extractBackTests() {
		assertEquals(extractBack(null, '.'), "");
		assertEquals(extractBack("", '.'), "");
		assertEquals(extractBack("a.b.c", '.'), "c");
		assertEquals(extractBack("....c", '.'), "c");
		assertEquals(extractBack("c.", '.'), "");
	}

	@Test
	public void toStrForStr() {
		String a = "a";

		StringBuilder sb = start(a);
		String out = end(sb);
		assertEquals(out, "String[]");

		sb = start(a);
		firstObj(sb, "a", a);
		addObj(sb, "b", "b");
		out = end(sb);
		assertEquals(out, "String[a=a, b=b]");

		sb = start(a);
		firstObj(sb, a);
		addObj(sb, "b");
		out = end(sb);
		assertEquals(out, "String[a, b]");
	}

	@Test
	public void toStrForInt() {
		Integer a = 1;

		StringBuilder sb = start(a);
		String out = end(sb);
		assertEquals(out, "Integer[]");

		sb = start(a);
		firstInt(sb, "a", a);
		addInt(sb, 2);
		addInt(sb, "c", 3);
		out = end(sb);
		assertEquals(out, "Integer[a=1, 2, c=3]");
	}

	@Test
	public void toStrForLong() {
		Long a = 1L;

		StringBuilder sb = start(a);
		String out = end(sb);
		assertEquals(out, "Long[]");

		sb = start(a);
		firstLong(sb, "a", a);
		addLong(sb, 2);
		addLong(sb, "c", 3);
		out = end(sb);
		assertEquals(out, "Long[a=1, 2, c=3]");
	}

	@Test
	public void count() {
		assertEquals(StringUtil.count(null, ' '), 0);
		assertEquals(StringUtil.count("", ' '), 0);
		assertEquals(StringUtil.count("a", ' '), 0);
		assertEquals(StringUtil.count("a ", ' '), 1);
		assertEquals(StringUtil.count(" a", ' '), 1);
		assertEquals(StringUtil.count("a b", ' '), 1);
		assertEquals(StringUtil.count("a  b", ' '), 2);
		assertEquals(StringUtil.count("a b c", ' '), 2);
	}

	@Test
	public void split() {
		String[] abc = { "abc" };
		String[] a_b = { "a", "b" };

		assertEquals(StringUtil.split(null, ' '), StringUtil.EMPTY_STRING_ARRAY);
		assertEquals(StringUtil.split("", ' '), StringUtil.EMPTY_STRING_ARRAY);
		assertEquals(StringUtil.split(" ", ' '), StringUtil.EMPTY_STRING_ARRAY);
		assertEquals(StringUtil.split("   ", ' '), StringUtil.EMPTY_STRING_ARRAY);
		assertEquals(StringUtil.split("abc", ' '), abc);
		assertEquals(StringUtil.split("abc ", ' '), abc);
		assertEquals(StringUtil.split(" abc", ' '), abc);
		assertEquals(StringUtil.split("a b", ' '), a_b);
		assertEquals(StringUtil.split("a  b", ' '), a_b);
		assertEquals(StringUtil.split(" a  b ", ' '), a_b);
	}
}
