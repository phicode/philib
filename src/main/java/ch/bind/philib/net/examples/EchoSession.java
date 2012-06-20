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

package ch.bind.philib.net.examples;

import java.io.IOException;
import java.nio.ByteBuffer;

import ch.bind.philib.io.EndianConverter;
import ch.bind.philib.net.PureSessionBase;
import ch.bind.philib.validation.Validation;

public class EchoSession extends PureSessionBase {

	private long lastInteractionNs;

	private final boolean server;

	private long nextValueRead;

	private long nextValueWrite;

	private byte[] partial = new byte[8];

	private int partialSize;

	EchoSession(boolean server) {
		this.server = server;
	}

	@Override
	public void receive(ByteBuffer data) throws IOException {
		lastInteractionNs = System.nanoTime();
		assert (data.position() == 0);

		if (server) {
			send(data);
		} else {
			verifyReceived(data);
			assert (data.position() == data.limit());
		}
		// send(data);
	}

	private void verifyReceived(ByteBuffer data) {
		int rem = data.remaining();
		assert (rem > 0);
		if (partialSize > 0) {
			// partial data to be processed
			int partialRem = 8 - partialSize;
			if (rem < partialRem) {
				data.get(partial, partialSize, rem);
				partialSize += rem;
				assert (partialSize < 8);
				return;
			} else {
				data.get(partial, partialSize, partialRem);
				verify();
			}
		}
		while (rem >= 8) {
			data.get(partial);
			verify();
			rem -= 8;
		}
		if (rem > 0) {
			data.get(partial, 0, rem);
			partialSize = rem;
		} else {
			partialSize = 0;
		}
	}

	private void verify() {
		long v = EndianConverter.decodeInt64LE(partial);
		Validation.isTrue(v == nextValueRead);
		nextValueRead++;
	}

	public void send(int numBytes) {
		assert (numBytes % 8 == 0);

	}

	@Override
	public void closed() {
		System.out.printf("closed() rx=%d, tx=%d%n", getRx(), getTx());
	}

	public long getLastInteractionNs() {
		return lastInteractionNs;
	}
}
