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
package ch.bind.philib.net.core.session;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.io.BufferUtil;
import ch.bind.philib.io.EndianConverter;
import ch.bind.philib.lang.ServiceState;
import ch.bind.philib.net.core.Connection;
import ch.bind.philib.net.core.Events;
import ch.bind.philib.net.core.Session;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class EchoClientSession implements Session {

	private static final Logger LOG = LoggerFactory.getLogger(EchoClientSession.class);

	private static final int NUMS = 1024;

	private static final int BUF_SIZE = NUMS * 8;

	private long lastInteractionNs;

	private final boolean performVerification;

	private final Connection connection;

	private long lastRx;

	private long lastTx;

	private final byte[] encodeBuf = new byte[BUF_SIZE];

	private final ByteBuffer writeBuf = ByteBuffer.wrap(encodeBuf);

	private long nextSendNum;

	private long nextReceiveNum;

	private ByteBuffer receiveBuf = ByteBuffer.allocate(BUF_SIZE);

	private byte[] decodeBuf = new byte[8];

	private boolean verificationOk = true;

	private final ServiceState state = new ServiceState();

	public EchoClientSession(Connection connection, boolean performVerification) {
		state.setOpen();
		this.connection = connection;
		this.performVerification = performVerification;
		writeBuf.limit(0);
		receiveBuf.limit(0);
	}

	public Connection getConnection() {
		return connection;
	}

	@Override
	public Events receive(Connection conn, ByteBuffer data) throws IOException {
		lastInteractionNs = System.nanoTime();
		if (performVerification) {
			receiveBuf = BufferUtil.append(receiveBuf, data);
			if (!verifyReceived()) {
				connection.close();
				return null;
			}
		}
		return send();
	}

	public void shutdown() {
		if (state.isOpen()) {
			state.setClosing();
			connection.setTimeout(10000L);
		}
	}

	@Override
	public boolean handleTimeout() {
		return false;
	}

	private boolean verifyReceived() throws IOException {
		while (receiveBuf.remaining() >= 8) {
			receiveBuf.get(decodeBuf);
			long num = EndianConverter.decodeInt64LE(decodeBuf);
			if (num != nextReceiveNum) {
				verificationOk = false;
				LOG.error("expected: " + nextReceiveNum + " got: " + num);
				return false;
			}
			nextReceiveNum++;
		}
		return true;
	}

	private Events send() throws IOException {
		if (writeBuf.hasRemaining()) {
			connection.send(writeBuf);
		}
		if (state.isClosingOrClosed()) {
			if (connection.getTx() == connection.getRx()) {
				return null; // close the connection
			}
			return Events.RECEIVE;
		}
		while (!writeBuf.hasRemaining()) {
			if (performVerification) {
				fillWriteBuf();
			} else {
				writeBuf.clear();
			}
			connection.send(writeBuf);
		}
		return Events.SENDABLE_RECEIVE;
	}

	private void fillWriteBuf() {
		for (int i = 0, off = 0; i < NUMS; i++, off += 8) {
			EndianConverter.encodeInt64LE(nextSendNum++, encodeBuf, off);
		}
		writeBuf.clear();
	}

	@Override
	public void closed(Connection conn) {
		state.setClosed();
	}

	public long getLastInteractionNs() {
		return lastInteractionNs;
	}

	@Override
	public Events sendable(Connection conn) throws IOException {
		lastInteractionNs = System.nanoTime();
		return send();
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

	public boolean isVerificationOk() {
		return verificationOk;
	}

	public boolean isClosed() {
		return state.isClosed();
	}
}