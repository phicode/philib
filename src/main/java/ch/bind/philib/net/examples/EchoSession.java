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
import ch.bind.philib.lang.ArrayUtil;
import ch.bind.philib.net.PureSessionBase;
import ch.bind.philib.validation.Validation;

public class EchoSession extends PureSessionBase {

	private long lastInteractionNs;

	private final boolean server;

	// private long numRead;
	//
	// private long numWrite;
	//
	// private long transitNums;

	private long nextValueRead;

	private long nextValueSend;

	private final byte[] readBuf = new byte[8];

	private final ByteBuffer writeBb = ByteBuffer.wrap(new byte[8192]);

	private final byte[] sendEnc = new byte[8];

	// private AtomicInteger maxInTransit = new AtomicInteger();

	private final Object lock = new Object();

	private int numSendable;

	// private int partialSize;

	EchoSession(boolean server) {
		this.server = server;
		// the write buffer starts in write mode, but there is nothing to write,
		// so tell it that there is nothing to write
		writeBb.limit(0);
	}

	@Override
	public void receive(ByteBuffer data) throws IOException {
//		System.out.println("received: " + ArrayUtil.formatShortHex(data));
		lastInteractionNs = System.nanoTime();
		assert (data.position() == 0);

		if (server) {
			// the server only performs data echoing
			sendAsync(data);
		} else {
			synchronized (lock) {
				verifyReceived(data);
				_send();
			}
		}
	}

	public void send() throws IOException {
		synchronized (lock) {
			_send();
		}
	}

	void _send() throws IOException {
		// writeBb is in read mode
		if (writeBb.hasRemaining()) {
			System.out.println("aaa\nsending: " + ArrayUtil.formatShortHex(writeBb));
			sendAsync(writeBb);
			if (writeBb.hasRemaining()) {
				return;
			}
		}
		long loops = 0;
		do {
			// switch writeBb to write mode
			writeBb.clear();
			int canWriteNums = writeBb.capacity() / 8;
			int sendNums = Math.min(numSendable, canWriteNums);
			Validation.isTrue(sendNums >= 0);
			if (sendNums == 0) {
				return;
			}
			enc(sendNums);
			// switch writeBb to read mode
			writeBb.flip();
			Validation.isTrue(writeBb.position() == 0 && writeBb.limit() == sendNums * 8, "" + writeBb.limit());
			System.out.println("bbb\nsending: " + ArrayUtil.formatShortHex(writeBb));
			sendAsync(writeBb);
			loops++;
			if (loops > 10 && !writeBb.hasRemaining()) {
				System.out.println("wrote " + (writeBb.capacity() * loops) + " in one go!");
			}
		} while (!writeBb.hasRemaining()); // loop if all data has been written
	}

	private void enc(int num) {
		for (int i = 0; i < num; i++) {
			EndianConverter.encodeInt64LE(nextValueSend, sendEnc);
			writeBb.put(sendEnc);
			numSendable--;
			nextValueSend++;
		}
	}

	private void verifyReceived(ByteBuffer data) {
		int rem = data.remaining();
		assert (rem > 0);
		while (rem >= 8) {
			data.get(readBuf);
			verify();
			rem -= 8;
		}
	}

	private void verify() {
		long v = EndianConverter.decodeInt64LE(readBuf);
		if (v != nextValueRead) {
			throw new AssertionError(v + " != " + nextValueRead);
		}
		nextValueRead++;
		numSendable++;
	}

	@Override
	public void closed() {
		System.out.printf("closed() rx=%d, tx=%d%n", getRx(), getTx());
	}

	public long getLastInteractionNs() {
		return lastInteractionNs;
	}

	public void incInTransitBytes(int num) {
		Validation.isTrue(num % 8 == 0);
		synchronized (lock) {
			numSendable += num / 8;
		}
	}
}
