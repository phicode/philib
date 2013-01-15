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
package wip.src.net.core.events;

import java.nio.channels.SelectionKey;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public final class SelectOps {

	private SelectOps() {
	}

	public static final int READ = SelectionKey.OP_READ;

	public static final int WRITE = SelectionKey.OP_WRITE;

	public static final int READ_WRITE = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

	public static final int ACCEPT = SelectionKey.OP_ACCEPT;

	public static final int CONNECT = SelectionKey.OP_CONNECT;

	// public static String opsToString(int ops) {
	// switch (ops) {
	// case READ:
	// return "r";
	// case WRITE:
	// return "w";
	// case READ_WRITE:
	// return "rw";
	// case ACCEPT:
	// return "a";
	// case CONNECT:
	// return "c";
	// default:
	// return Integer.toString(ops);
	// }
	// }

	public static boolean hasRead(int events) {
		return (events & READ) == READ;
	}

	public static boolean hasWrite(int events) {
		return (events & WRITE) == WRITE;
	}

	public static boolean hasConnect(int events) {
		return (events & CONNECT) == CONNECT;
	}
}
