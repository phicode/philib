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

	private static final String DEFAULT_COUNTER_NAME = "default";

	private static final String DEFAULT_UNIT_NAME = "unknown";

	public static final CounterRepo DEFAULT = new CounterRepo();

	private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<String, Counter>();

	public CounterRepo() {
	}

	public Counter forClass(Class<?> clazz) {
		return forClass(clazz, null);
	}

	public Counter forClass(Class<?> clazz, String unit) {
		String name = clazz == null ? null : clazz.getName();
		return forName(name, unit);
	}

	public Counter forName(String name) {
		return forName(name, null);
	}

	public Counter forName(String name, String unit) {
		name = getOrElse(name, DEFAULT_COUNTER_NAME);
		unit = getOrElse(unit, DEFAULT_UNIT_NAME);
		Counter counter = counters.get(name);
		if (counter != null) {
			return counter;
		}
		counter = new Counter(name, unit);
		Counter other = counters.putIfAbsent(name, counter);
		return other != null ? other : counter;
	}

	public void remove(String name) {
		counters.remove(getOrElse(name, DEFAULT_COUNTER_NAME));
	}

	public Collection<Counter> getCounters() {
		return new ArrayList<Counter>(counters.values());
	}

	private String getOrElse(String name, String def) {
		return (name == null || name.isEmpty()) ? def : name;
	}
}
