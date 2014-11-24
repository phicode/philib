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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class MurmurHashTest {

	private static final Logger LOG = LoggerFactory.getLogger(MurmurHashTest.class);

	@Test
	public void murmurWordlist() {
		MurmurHash.optimize();
		List<String> wordlist = TestUtil.getWordlist();

		Map<Integer, String> murmur2hashes = new HashMap<>(wordlist.size(), 1f);
		Map<Integer, String> murmur2ahashes = new HashMap<>(wordlist.size(), 1f);
		Map<Integer, String> murmur3hashes = new HashMap<>(wordlist.size(), 1f);

		List<String> dups2 = new LinkedList<>();
		List<String> dups2a = new LinkedList<>();
		List<String> dups3 = new LinkedList<>();
		for (String s : wordlist) {
			int m2 = MurmurHash.murmur2(s.getBytes());
			int m2a = MurmurHash.murmur2a(s.getBytes());
			int m3 = MurmurHash.murmur3(s.getBytes());

			String existing2 = murmur2hashes.put(m2, s);
			String existing2a = murmur2ahashes.put(m2a, s);
			String existing3 = murmur3hashes.put(m3, s);
			if (existing2 != null) {
				dups2.add(String.format("%d => '%s' & '%s'", m2, existing2, s));
			}
			if (existing2a != null) {
				dups2a.add(String.format("%d => '%s' & '%s'", m2a, existing2a, s));
			}
			if (existing3 != null) {
				dups3.add(String.format("%d => '%s' & '%s'", m3, existing3, s));
			}
		}
		assertTrue(dups2.size() < 3);
		assertTrue(dups2a.size() < 5);
		assertTrue(dups3.size() < 3);

		LOG.debug("murmur2 duplicates: " + dups2);
		LOG.debug("murmur2a duplicates: " + dups2a);
		LOG.debug("murmur3 duplicates: " + dups3);
	}
}
