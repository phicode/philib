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
package ch.bind.philib.lang;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public abstract class ExceptionUtil {

	protected ExceptionUtil() {
	}

	public static String buildMessageChain(Throwable t) {
		if (t == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(128);
		add(sb, t);
		t = t.getCause();
		while (t != null) {
			// separator between causes;
			sb.append(" => ");
			add(sb, t);
			t = t.getCause();
		}
		return sb.toString();
	}

	private static void add(StringBuilder sb, Throwable t) {
		sb.append(t.getClass().getSimpleName());
		StackTraceElement[] trace = t.getStackTrace();
		if (trace != null && trace.length > 0) {
			sb.append('@');
			sb.append(trace[0].toString());
		} else {
			sb.append("@no-stack-trace");
		}
		String msg = t.getMessage();
		if (msg == null) {
			sb.append("()");
		} else {
			sb.append('(');
			sb.append(msg);
			sb.append(')');
		}
	}
}
