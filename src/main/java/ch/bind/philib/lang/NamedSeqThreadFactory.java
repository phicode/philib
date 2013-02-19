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

package ch.bind.philib.lang;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.validation.Validation;

/**
 * A thread factory which generates thread names of the form
 * &lt;name&gt;-&lt;sequence&gt;
 * 
 * @author philipp meinen
 */
public final class NamedSeqThreadFactory implements ThreadFactory {

	private final AtomicLong SEQ = new AtomicLong(0);

	private final String name;

	/**
	 * @see NamedSeqThreadFactory
	 * @param name The name which must be used for newly created threads.
	 */
	public NamedSeqThreadFactory(String name) {
		Validation.notNull(name);
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable r) {
		String threadname = name + "-" + SEQ.getAndIncrement();
		return new Thread(r, threadname);
	}
}
