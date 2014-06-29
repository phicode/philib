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

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.CompareUtil;
import ch.bind.philib.util.CowSet;
import ch.bind.philib.validation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * @author Philipp Meinen
 */
public final class Config {

	private final CowSet<ConfigListener> listeners = new CowSet<ConfigListener>(ConfigListener.class);

	private final List<URL> urls = new LinkedList<URL>();

	private boolean loading;

	private volatile Map<String, String> config;

	public Config(URL url) {
		setURL(url);
	}

	public Config(URL[] urls) {
		setURLs(urls);
	}

	public Config(Collection<URL> urls) {
		setURLs(urls);
	}

	public Config(Map<String, String> config) {
		this.config = new HashMap<String, String>(config);
	}

	public synchronized void setURL(URL url) {
		Validation.notNull(url);
		urls.clear();
		urls.add(url);
	}

	public synchronized void setURLs(URL[] urls) {
		Validation.notNullOrEmpty(urls);
		this.urls.clear();
		Collections.addAll(this.urls, urls);
	}

	public synchronized void setURLs(Collection<URL> urls) {
		Validation.notNullOrEmpty(urls);
		this.urls.clear();
		for (URL url : urls) {
			this.urls.add(url);
		}
	}

	public void addListener(ConfigListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ConfigListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Loads all configuration urls. At least one URL
	 *
	 * @throws IOException in case no url could be opened.
	 */
	public synchronized void load() throws IOException {
		if (loading || urls.isEmpty()) {
			return;
		}
		loading = true;
		int numSuccess = 0;
		Exception lastExc = null;
		Map<String, String> newConfig = new HashMap<String, String>();
		try {
			for (URL url : urls) {
				if (url == null) {
					continue;
				}
				InputStream is = null;
				try {
					is = url.openStream();
					Properties props = new Properties();
					props.load(is);
					Map<String, String> m = toMap(props);
					newConfig.putAll(m);
					numSuccess++;
				} catch (IOException e) {
					lastExc = e;
				} finally {
					SafeCloseUtil.close(is);
				}
			}

			if (numSuccess < 1) {
				throw new IOException("no resources found", lastExc);
			}
			if (config != null) {
				notifyDifferences(newConfig, config);
			} else {
				notifyAdded(newConfig);
			}
			config = newConfig;
		} finally {
			loading = false;
		}
	}

	public static Map<String, String> toMap(Properties p) {
		Map<String, String> m = new HashMap<String, String>();
		for (String key : p.stringPropertyNames()) {
			m.put(key, p.getProperty(key));
		}
		return m;
	}

	private void notifyDifferences(Map<String, String> newConfig, Map<String, String> oldConfig) {
		if (listeners.isEmpty()) {
			return;
		}
		Set<String> newKeys = newConfig.keySet();
		Set<String> oldKeys = oldConfig.keySet();
		for (String newKey : newKeys) {
			if (oldKeys.contains(newKey)) {
				String valNew = newConfig.get(newKey);
				String valOld = oldConfig.get(newKey);
				if (!CompareUtil.equals(valNew, valOld)) {
					notifyChanged(newKey, valOld, valNew);
				}
			} else {
				notifyAdded(newKey, newConfig.get(newKey));
			}
		}
		for (String key : oldKeys) {
			if (!newKeys.contains(key)) {
				notifyRemoved(key, oldConfig.get(key));
			}
		}
	}

	private void notifyAdded(Map<String, String> m) {
		if (listeners.isEmpty()) {
			return;
		}
		for (Entry<String, String> e : m.entrySet()) {
			notifyAdded(e.getKey(), e.getValue());
		}
	}

	private void notifyAdded(String key, String value) {
		for (ConfigListener l : listeners.getView()) {
			l.added(key, value);
		}
	}

	private void notifyRemoved(String key, String oldValue) {
		for (ConfigListener l : listeners.getView()) {
			l.removed(key, oldValue);
		}
	}

	private void notifyChanged(String key, String oldValue, String newValue) {
		for (ConfigListener l : listeners.getView()) {
			l.changed(key, oldValue, newValue);
		}
	}

	public String get(String key) {
		Map<String, String> c = config;
		return c == null ? null : c.get(key);
	}

	public String get(String key, String def) {
		String v = get(key);
		return v == null ? def : v;
	}

	public Integer getInt(String key) {
		String v = get(key);
		return v == null ? null : Integer.parseInt(v);
	}

	public int getInt(String key, int def) {
		String v = get(key);
		return v == null ? def : Integer.parseInt(v);
	}

	public Long getLong(String key) {
		String v = get(key);
		return v == null ? null : Long.parseLong(v);
	}

	public long getLong(String key, long def) {
		String v = get(key);
		return v == null ? def : Long.parseLong(v);
	}

	public Boolean getBool(String key) {
		String v = get(key);
		return v == null ? null : Boolean.parseBoolean(v);
	}

	public boolean getBool(String key, boolean def) {
		String v = get(key);
		return v == null ? def : Boolean.parseBoolean(v);
	}

	public Double getDouble(String key) {
		String v = get(key);
		return v == null ? null : Double.parseDouble(v);
	}

	public double getDouble(String key, double def) {
		String v = get(key);
		return v == null ? def : Double.parseDouble(v);
	}
}
