/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class CounterRepo {

	private static final String DEFAULT_NAME = "default";

	private static final char POSTFIX_SEP = ':';

	public static final CounterRepo DEFAULT = new CounterRepo();

	private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<String, Counter>();

	public CounterRepo() {}

	public Counter forClass(Class<?> clazz) {
		return get(nameOf(clazz));
	}

	public Counter forClass(Class<?> clazz, String postfix) {
		return get(nameOf(clazz, postfix));
	}

	public Counter forName(String name) {
		return get(nameOf(name));
	}

	public Counter forName(String name, String postfix) {
		return get(nameOf(name, postfix));
	}

	private Counter get(String realName) {
		Counter counter = counters.get(realName);
		if (counter != null) {
			return counter;
		}
		counter = new Counter(realName);
		Counter other = counters.putIfAbsent(realName, counter);
		return other != null ? other : counter;
	}

	public void remove(String name) {
		counters.remove(nameOf(name));
	}

	public void remove(String name, String postfix) {
		counters.remove(nameOf(name, postfix));
	}

	public void remove(Class<?> clazz) {
		counters.remove(nameOf(clazz));
	}

	public void remove(Class<?> clazz, String postfix) {
		counters.remove(nameOf(clazz, postfix));
	}

	public Collection<Counter> getCounters() {
		return new ArrayList<Counter>(counters.values());
	}

	public void clear() {
		counters.clear();
	}

	private static String nameOf(String name) {
		return nullOrEmpty(name) ? DEFAULT_NAME : name;
	}

	private static String nameOf(String name, String postfix) {
		String n = nameOf(name);
		return nullOrEmpty(postfix) ? n : n + POSTFIX_SEP + postfix;
	}

	private static String nameOf(Class<?> clazz) {
		return clazz == null ? DEFAULT_NAME : clazz.getName();
	}

	private static String nameOf(Class<?> clazz, String postfix) {
		String n = nameOf(clazz);
		return nullOrEmpty(postfix) ? n : n + POSTFIX_SEP + postfix;
	}

	private static final boolean nullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}
}
