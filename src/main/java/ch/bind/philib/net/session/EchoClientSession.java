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
package ch.bind.philib.net.session;

import java.io.IOException;
import java.nio.ByteBuffer;

import ch.bind.philib.io.EndianConverter;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Session;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class EchoClientSession implements Session {

	private long lastInteractionNs;

	private long nextValueRead;

	private long nextValueSend;

	private final byte[] readBuf = new byte[8192];

	private final ByteBuffer writeBb = ByteBuffer.wrap(new byte[8192]);

	private final byte[] sendEnc = new byte[8];

	private final Object lock = new Object();

	private final boolean performVerification;

	private final Connection connection;

	private boolean sendPending = false;

	private int numSendable = 16384;

	private long lastRx;

	private long lastTx;

	public EchoClientSession(Connection connection, boolean performVerification) {
		this.connection = connection;
		this.performVerification = performVerification;
	}

	public Connection getConnection() {
		return connection;
	}

	@Override
	public void receive(ByteBuffer data) throws IOException {
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
		}
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
		int numVerify = Math.min(readBuf.length / 8, rem / 8);
		int verifyBytes = numVerify * 8;
		data.get(readBuf, 0, verifyBytes);
		int off = 0;
		while (off < verifyBytes) {
			long v = EndianConverter.decodeInt64LE(readBuf, off);
			off += 8;
			if (v != nextValueRead) {
				System.out.println("expected: " + nextValueRead + " got: " + v);
				// throw new AssertionError(v + " != " + nextValueRead);
			}
			nextValueRead++;
			numSendable++;
		}
	}

	@Override
	public void closed() {
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
	public void writable() throws IOException {
		send();
	}

	public long getRxDiff() {
		long rx = connection.getRx();
		long diff = rx - lastRx;
		lastRx = rx;
		return diff;
	}

	public long getTxDiff() {
		long tx = connection.getTx();
		long diff = tx - lastTx;
		lastTx = tx;
		return diff;
	}

	@Override
	public String toString() {
		return String.format("%s[rx=%d, tx=%d, remote=%s]", getClass().getSimpleName(), connection.getRx(), connection.getTx(), connection.getRemoteAddress());
	}
}
