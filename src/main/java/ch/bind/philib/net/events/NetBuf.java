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
package ch.bind.philib.net.events;

import java.nio.ByteBuffer;

public abstract class NetBuf {

	private final ByteBuffer bb;

	private boolean pending = true;

	private NetBuf(final ByteBuffer bb) {
		this.bb = bb;
	}

	abstract boolean isIntern();

	public final ByteBuffer getBuffer() {
		return bb;
	}

	public final void finished() {
		pending = false;
	}

	public final boolean isPending() {
		return pending;
	}

	public static NetBuf createIntern(final ByteBuffer bb) {
		return new InternBuf(bb);
	}

	public static NetBuf createExtern(final ByteBuffer bb) {
		return new ExternBuf(bb);
	}

	private static final class InternBuf extends NetBuf {

		InternBuf(ByteBuffer bb) {
			super(bb);
		}

		@Override
		boolean isIntern() {
			return true;
		}
	}

	private static final class ExternBuf extends NetBuf {

		ExternBuf(ByteBuffer bb) {
			super(bb);
		}

		@Override
		boolean isIntern() {
			return false;
		}
	}
}
