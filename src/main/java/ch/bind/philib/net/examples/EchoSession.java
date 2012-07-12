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
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Session;
import ch.bind.philib.validation.Validation;

public class EchoSession implements Session {

	private long lastInteractionNs;

	private final boolean server;

	private long nextValueRead;

	private long nextValueSend;

	private final byte[] readBuf = new byte[8];

	private final ByteBuffer writeBb = ByteBuffer.wrap(new byte[8192]);

	private boolean sendPending = false;

	private final byte[] sendEnc = new byte[8];

	private final Object lock = new Object();

	private int numSendable;

	private final boolean performVerification;

	final Connection connection;

	EchoSession(Connection connection, boolean server, boolean performVerification) {
		this.connection = connection;
		this.server = server;
		this.performVerification = performVerification;
	}

	@Override
	public void receive(ByteBuffer data) throws IOException {
		assert (data.position() == 0);

		if (server) {
			// the server only performs data echoing
			if (connection.sendAsync(data) > 0) {
				lastInteractionNs = System.nanoTime();
			}
		} else {
			lastInteractionNs = System.nanoTime();
			synchronized (lock) {
				if (performVerification) {
					verifyReceived(data);
				} else {
					int rem = data.remaining();
					int consume = (rem / 8);
					numSendable += consume;
					int newPos = data.position() + (consume * 8);
					data.position(newPos);
				}
				// TODO: send nulled packets if we are not in verification mode
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
		if (sendPending) {
			// writeBb is in read mode
			connection.sendAsync(writeBb);
			if (writeBb.hasRemaining()) {
				return;
			}
			sendPending = false;
		}
		long loops = 0;
		while (!sendPending) {// loop if all data has been written
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
			connection.sendAsync(writeBb);
			sendPending = writeBb.hasRemaining();
			loops++;
			if (loops > 10 && !writeBb.hasRemaining()) {
				System.out.println("wrote " + (writeBb.capacity() * loops) + " in one go!");
			}
		}
	}

	private void enc(int num) {
		// System.out.println("sending:  " + nextValueSend + "-" +
		// (nextValueSend + num - 1));
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
		System.out.printf("closed() rx=%d, tx=%d%n", connection.getRx(), connection.getTx());
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

	@Override
	public void writable() {
		// TODO
		try {
			send();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	public String getDebugInformations() {
		return connection.getDebugInformations();
	}
}
