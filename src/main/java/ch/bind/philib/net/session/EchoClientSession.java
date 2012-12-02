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

import ch.bind.philib.io.BufferUtil;
import ch.bind.philib.io.EndianConverter;
import ch.bind.philib.io.RingBuffer;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.InterestedEvents;
import ch.bind.philib.net.Session;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class EchoClientSession implements Session {

	private long lastInteractionNs;

	private final boolean performVerification;

	private final Connection connection;

	private long lastRx;

	private long lastTx;

	private final RingBuffer buf = new RingBuffer();
	private final byte[] encodeBuf = new byte[8192];
	private final ByteBuffer writeBuf = ByteBuffer.wrap(encodeBuf);
	private long nextSendNum;
	private long nextReceiveNum;

	private ByteBuffer receiveBuf = ByteBuffer.allocate(8192);
	private byte[] receiveNum = new byte[8];

	public EchoClientSession(Connection connection, boolean performVerification) {
		this.connection = connection;
		this.performVerification = performVerification;
	}

	public Connection getConnection() {
		return connection;
	}

	@Override
	public InterestedEvents receive(Connection conn, ByteBuffer data) throws IOException {
		lastInteractionNs = System.nanoTime();
		if (performVerification) {
			receiveBuf = BufferUtil.append(receiveBuf, data);
			verifyReceived();
		}
		send();
		return InterestedEvents.SENDABLE_RECEIVE;
	}

	private void verifyReceived() {
		while (receiveBuf.remaining() >= 8) {
			receiveBuf.get(receiveNum);
			long num = EndianConverter.decodeInt64LE(receiveNum);
			if (num != nextReceiveNum) {
				System.out.println("expected: " + nextReceiveNum + " got: " + num);
				// throw new AssertionError(num + " != " + nextReceiveNum);
			}
			nextReceiveNum++;
		}
	}

	private void send() throws IOException {
		if (writeBuf.hasRemaining()) {
			connection.send(writeBuf);
		}
		while (!writeBuf.hasRemaining()) {
			if (performVerification) {
				fillWriteBuf();
			} else {
				writeBuf.clear();
			}
			connection.send(writeBuf);
		}
	}

	private void fillWriteBuf() {
		int canWriteNums = writeBuf.capacity() / 8;
		for (int i = 0, off = 0; i < canWriteNums; i++, off += 8) {
			EndianConverter.encodeInt64LE(nextSendNum++, encodeBuf, off);
		}
		writeBuf.clear();
	}

	@Override
	public void closed(Connection conn) {
	}

	public long getLastInteractionNs() {
		return lastInteractionNs;
	}

	@Override
	public InterestedEvents sendable(Connection conn) throws IOException {
		lastInteractionNs = System.nanoTime();
		send();
		return InterestedEvents.SENDABLE_RECEIVE;
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
		return String.format("%s[rx=%d, tx=%d, remote=%s]", getClass().getSimpleName(), connection.getRx(), connection.getTx(),
				connection.getRemoteAddress());
	}
}
