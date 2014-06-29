/*
 * Copyright (c) 2014 Philipp Meinen <philipp@bind.ch>
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingListener implements ConfigListener {

	private final Logger log;

	public LoggingListener() {
		this(LoggerFactory.getLogger(LoggingListener.class));
	}

	public LoggingListener(Logger log) {
		this.log = log;
	}

	@Override
	public void changed(String key, String oldValue, String newValue) {
		log.info("changed '%s': '%s' -> '%s'", key, oldValue, newValue);
	}

	@Override
	public void removed(String key, String oldValue) {
		log.info("removed '%s': '%s'", key, oldValue);
	}

	@Override
	public void added(String key, String value) {
		log.info("added '%s': '%s'", key, value);
	}

	public Logger getLogger() {
		return log;
	}
}
