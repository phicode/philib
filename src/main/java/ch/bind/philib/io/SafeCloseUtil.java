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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.reflect.Method;

/**
 * @author Philipp Meinen
 */
public abstract class SafeCloseUtil {

	private static final Logger LOG = LoggerFactory.getLogger(SafeCloseUtil.class);

	protected SafeCloseUtil() {
	}

	public static void close(Closeable closeable) {
		close(closeable, LOG);
	}

	public static void close(Object obj) {
		close(obj, LOG);
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
		} catch (Exception e) {
			logger.error("error while closing a resource: " + e.getMessage(), e);
		}
	}

	public static void close(Object obj, Logger logger) {
		if (obj == null) {
			return;
		}
		if (obj instanceof Closeable) {
			close((Closeable) obj, logger);
			return;
		}
		if (logger == null) {
			logger = LOG;
		}
		Method closeMethod;
		try {
			closeMethod = obj.getClass().getMethod("close");
		} catch (NoSuchMethodException e) {
			logger.warn("close method not found on class: " + obj.getClass().getName());
			return;
		} catch (SecurityException e) {
			logger.warn("cannot access close method on class: " + obj.getClass().getName());
			return;
		}
		try {
			closeMethod.invoke(obj);
		} catch (Exception e) {
			logger.error("error while closing a resource: " + e.getMessage(), e);
		}
	}
}
