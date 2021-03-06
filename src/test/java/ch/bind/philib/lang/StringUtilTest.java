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

import ch.bind.philib.TestUtil;
import ch.bind.philib.test.Bench;
import ch.bind.philib.test.Bencher;
import org.testng.annotations.Test;

import static ch.bind.philib.lang.StringUtil.extractBack;
import static org.testng.Assert.assertEquals;

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
	public void countBench() throws InterruptedException {
		if (TestUtil.RUN_BENCHMARKS) {
			for (int i = 16; i <= 1024; i *= 2) {
				Bench.runAndPrint(new CountBencher(i));
			}
		}
	}

	private static final class CountBencher implements Bencher {

		private final String str;

		public CountBencher(int len) {
			StringBuilder sb = new StringBuilder(len);
			for (int i = 0; i < len; i++) {
				sb.append('.');
			}
			this.str = sb.toString();
		}

		@Override
		public void run(long loops) {
			String s = str;
			long total = 0;
			for (long i = 0; i < loops; i++) {
				total += StringUtil.count(s, '.');
			}
			assertEquals(total, s.length() * loops);
		}

		@Override
		public String getName() {
			return "StringUtil.count-" + str.length();
		}
	}

	@Test
	public void split() {
		String[] abc = {"abc"};
		String[] a_b = {"a", "b"};

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

	@Test
	public void splitBench() throws InterruptedException {
		if (TestUtil.RUN_BENCHMARKS) {
			for (int i = 16; i <= 1024; i *= 2) {
				Bench.runAndPrint(new SplitBencher(i));
			}
		}
	}

	private static final class SplitBencher implements Bencher {

		private final String str;

		public SplitBencher(int len) {
			StringBuilder sb = new StringBuilder(len);
			for (int i = 0; i < len; i += 2) {
				sb.append(".a");
			}
			this.str = sb.toString();
		}

		@Override
		public void run(long loops) {
			long total = 0;
			for (long i = 0; i < loops; i++) {
				total += StringUtil.split(str, '.').length;
			}
			assertEquals(total, str.length() * loops / 2);
		}

		@Override
		public String getName() {
			return "StringUtil.split-" + str.length();
		}
	}
}
