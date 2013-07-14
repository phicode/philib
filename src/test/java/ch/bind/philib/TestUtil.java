/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.bind.philib.io.SafeCloseUtil;

public class TestUtil {

	private static final long DEFAULT_SLEEPTIME_MS = 500;

	public static final boolean RUN_BENCHMARKS = "true".equalsIgnoreCase(System.getProperty("runBenchmarks"));

	private TestUtil() {
	}

	public static void gcAndSleep() {
		gcAndSleep(DEFAULT_SLEEPTIME_MS);
	}

	public static void gcAndSleep(long sleepTime) {
		System.gc();
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			throw new RuntimeException("interrupted while sleeping for a test!");
		}
	}

	public static void printBenchResults(Class<?> clazz, String description, String shortUnit, long timeNs, double amount) {
		assertTrue(timeNs > 0);
		double perS = amount / (timeNs / 1000000000f);
		double perMs = amount / (timeNs / 1000000f);
		System.out.printf("Bench [%-20s] %12.0f %-16s in %12d ns => %12.0f %-3s/s => %15.3f %-3s/ms%n", //
		        clazz.getSimpleName(), amount, description, timeNs, perS, shortUnit, perMs, shortUnit);
	}

	private static volatile SoftReference<List<String>> wordlist;

	public static List<String> getWordlist() {
		List<String> wl = wordlist == null ? null : wordlist.get();
		if (wl != null) {
			return wl;
		}
		ArrayList<String> words = new ArrayList<String>(256 * 1024);
		InputStream is = TestUtil.class.getResourceAsStream("/words_en");
		assertNotNull(is);
		try {
			InputStreamReader isr = new InputStreamReader(is, Charset.forName("US-ASCII"));
			BufferedReader rdr = new BufferedReader(isr, 32 * 1024);
			String line;
			while ((line = rdr.readLine()) != null) {
				words.add(line);
			}
			words.trimToSize();
			wl = Collections.unmodifiableList(words);
			wordlist = new SoftReference<List<String>>(wl);
			return wl;
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			SafeCloseUtil.close(is);
		}
		fail();
		return null;
	}

	public static void sleepOrFail(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			fail("interrupted while sleeping");
		}
	}
}
