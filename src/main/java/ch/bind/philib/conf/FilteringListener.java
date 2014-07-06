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

package ch.bind.philib.conf;

import ch.bind.philib.validation.Validation;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Philipp Meinen
 */
public final class FilteringListener implements ConfigListener {

	private final ConfigListener listener;

	private final Set<String> filteredProperties = new HashSet<>();

	private boolean blacklist = true;

	private boolean includeChanges = true;

	private boolean includeAdditions = true;

	private boolean includeRemovals = true;

	public FilteringListener(ConfigListener listener) {
		Validation.notNull(listener);
		this.listener = listener;
	}

	@Override
	public void changed(String key, String oldValue, String newValue) {
		if (includeChanges && !filterMatch(key)) {
			listener.changed(key, oldValue, newValue);
		}
	}

	@Override
	public void removed(String key, String oldValue) {
		if (includeRemovals && !filterMatch(key)) {
			listener.removed(key, oldValue);
		}
	}

	@Override
	public void added(String key, String value) {
		if (includeAdditions && !filterMatch(key)) {
			listener.added(key, value);
		}
	}

	public boolean isIncludeChanges() {
		return includeChanges;
	}

	public void setIncludeChanges(boolean includeChanges) {
		this.includeChanges = includeChanges;
	}

	public boolean isIncludeAdditions() {
		return includeAdditions;
	}

	public void setIncludeAdditions(boolean includeAdditions) {
		this.includeAdditions = includeAdditions;
	}

	public boolean isIncludeRemovals() {
		return includeRemovals;
	}

	public void setIncludeRemovals(boolean includeRemovals) {
		this.includeRemovals = includeRemovals;
	}

	public boolean isBlacklist() {
		return blacklist;
	}

	public void setBlacklist(boolean blacklist) {
		this.blacklist = blacklist;
	}

	public void addFilterProperty(String key) {
		filteredProperties.add(key);
	}

	public void removeFilterProperty(String key) {
		filteredProperties.remove(key);
	}

	public boolean filterMatch(String key) {
		boolean contained = filteredProperties.contains(key);
		return blacklist ? contained : !contained;
	}
}
