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
package ch.bind.philib.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class SafeCloseUtil {

	private static final Logger LOG = LoggerFactory.getLogger(SafeCloseUtil.class);

	private SafeCloseUtil() {}

	public static void close(Closeable closeable) {
		close(closeable, null);
	}

	public static void close(Closeable closeable, Logger logger) {
		if (closeable == null) {
			return;
		}
		if (logger == null) {
			logger = LOG;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			logger.error("error while closing an object: " + e.getMessage(), e);
		}
	}

	public static void close(Selector selector, Logger logger) {
		if (selector == null) {
			return;
		}
		if (logger == null) {
			logger = LOG;
		}
		try {
			selector.close();
		} catch (IOException e) {
			logger.error("error while closing a selector: " + e.getMessage(), e);
		}
	}
}
