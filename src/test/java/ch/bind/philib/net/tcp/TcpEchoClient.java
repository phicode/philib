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
package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionManager;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.context.NetContextImpl;
import ch.bind.philib.net.events.ConcurrentEventDispatcher;
import ch.bind.philib.net.events.EventDispatcher;
import ch.bind.philib.net.session.EchoClientSession;
import ch.bind.philib.pool.buffer.ByteBufferPool;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
// TODO: reply data validation
// TODO: latency measurements
// TODO: many threads
public class TcpEchoClient {

	private static final boolean VERIFY_MODE = false;

	private static final long CONNECT_TIMEOUT = 250L;

	public static void main(String[] args) throws Exception {
		int numClients = 1;
		// int rampUp =
		if (args.length > 1) {
			System.out.println("only one parameter may be specified");
			System.exit(1);
		}
		else if (args.length == 1) {
			try {
				numClients = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("not a number: " + args[0]);
				System.exit(1);
			}
			if (numClients < 1) {
				System.out.println("number of clients cant be less then 1");
				System.exit(1);
			}
		}
		new TcpEchoClient().run(numClients);
	}

	private List<RichEchoClientSession> sessions = new LinkedList<RichEchoClientSession>();

	private List<Future<TcpConnection>> connecting = new LinkedList<Future<TcpConnection>>();

	private long numConnectFails;

	private long numCloseFails;

	private InetSocketAddress endpoint;

	private NetContext context;

	private static final SessionManager sessionManager = new SessionManager() {

		@Override
		public Session createSession(Connection connection) throws IOException {
			return new EchoClientSession(connection, VERIFY_MODE);
			// session.send();
			// return session;
		}

		@Override
		public void connectFailed(SocketAddress remoteAddress, Throwable cause) {
			System.err.println("connect failed: " + ExceptionUtil.buildMessageChain(cause));
		}
	};

	// TODO: keep track of connection times
	private void run(int numClients) throws IOException {
		// endpoint = SocketAddresses.fromIp("10.0.0.71", 1234);
		// endpoint = SocketAddresses.fromIp("10.95.162.221", 1234);
		try {
			endpoint = SocketAddresses.fromIp("127.0.0.1", 1234);
		} catch (UnknownHostException e) {
			System.out.println("unknown host: " + ExceptionUtil.buildMessageChain(e));
			return;
		}
		ByteBufferPool bufferPool = ByteBufferPool.create(8192, 128, 3);
		EventDispatcher eventDispatcher = ConcurrentEventDispatcher.open(3);
		context = new NetContextImpl(sessionManager, bufferPool, eventDispatcher);
		// context.setTcpNoDelay(true);
		context.setSndBufSize(64 * 1024);
		context.setRcvBufSize(64 * 1024);
		// context.setConnectTimeout(CONNECT_TIMEOUT);
		// context.setTcpNoDelay(false);
		// context.setSndBufSize(512);
		// context.setRcvBufSize(512);

		final long printStatsIntervalMs = 10000;
		final long start = System.currentTimeMillis();

		// final long rampUpMs = 100L;
		// final long rampDownMs = 60000L;
		// final int maxConnections = 2000;

		final long rampUpMs = 250L;
		final long rampDownMs = 60000L;
		final int maxConnections = 100;

		long startNext = start - 1;
		long lastPrintStats = start;
		try {
			Thread self = Thread.currentThread();
			while (!self.isInterrupted()) {
				long now = System.currentTimeMillis();

				closeTooOld(rampDownMs);

				// periodically start new connections
				while (now > startNext) {
					maybeConnectOne(maxConnections);
					startNext += rampUpMs;
				}
				handleConnected();

				long tDiff = now - lastPrintStats;
				if (tDiff > printStatsIntervalMs) {
					printStats(printStatsIntervalMs, tDiff);
					lastPrintStats = now;
				}

				removeDisconnected();
				Thread.sleep((rampUpMs / 2) - 10);
			}
		} catch (InterruptedException e) {

		}
		SafeCloseUtil.close(context);
		System.exit(0);
	}

	private void removeDisconnected() {
		Iterator<RichEchoClientSession> iter = sessions.iterator();
		while (iter.hasNext()) {
			RichEchoClientSession recs = iter.next();
			if (!recs.session.getConnection().isConnected()) {
				iter.remove();
				close(recs);
			}
		}
	}

	private void close(RichEchoClientSession recs) {
		try {
			recs.session.getConnection().close();
		} catch (IOException e) {
			numCloseFails++;
			System.out.println("connection.close() failed: " + ExceptionUtil.buildMessageChain(e));
		}
	}

	private void printStats(long intervalMs, long timeDiff) {
		long totalRx = 0;
		long totalTx = 0;
		int num = sessions.size();
		Iterator<RichEchoClientSession> iter = sessions.iterator();
		while (iter.hasNext()) {
			RichEchoClientSession recs = iter.next();
			long rxDiff = recs.session.getRxDiff();
			long txDiff = recs.session.getTxDiff();
			totalRx += rxDiff;
			totalTx += txDiff;
			if (num == 1) {
				long diff = rxDiff + txDiff;
				double mbit = (diff * 8) / 1e6 / (timeDiff / 1000f);
				double rxMb = rxDiff / ((double) (1024 * 1024f));
				double txMb = txDiff / ((double) (1024 * 1024f));
				System.out.printf("last %dsec rx=%.3fM, tx=%.3fM bytes => %.5f mbit/sec rxTx=%d timeDiff=%d%n", //
						intervalMs / 1000, rxMb, txMb, mbit, (rxDiff + txDiff), timeDiff);
			}
		}
		if (num > 1) {
			double rxMb = totalRx / ((double) (1024 * 1024f));
			double txMb = totalTx / ((double) (1024 * 1024f));
			double mbit = ((totalRx + totalTx) * 8) / 1e6 / (timeDiff / 1000f);
			System.out.printf("last %dsec total-rx=%.3fM, total-tx=%.3fM bytes => %.5f mbit/sec, #connections: %d, connecting: %d, #handlers: %d%n", //
					intervalMs / 1000, rxMb, txMb, mbit, num, connecting.size(), context.getEventDispatcher().getNumEventHandlers());
		}
		if (sessions.isEmpty() && connecting.isEmpty()) {
			System.out.println("no active or connecting sessions!");
		}
	}

	private void handleConnected() throws InterruptedException {
		Iterator<Future<TcpConnection>> iter = connecting.iterator();
		while (iter.hasNext()) {
			Future<TcpConnection> conn = iter.next();
			if (conn.isDone()) {
				iter.remove();
				try {
					Session s = conn.get().getSession();
					sessions.add(new RichEchoClientSession((EchoClientSession) s));
				} catch (ExecutionException e) {
					numConnectFails++;
					System.out.println("exception while opening a connection async: " + ExceptionUtil.buildMessageChain(e));
				}
			}
		}
	}

	private void maybeConnectOne(int maxConnections) {
		if ((sessions.size() + connecting.size()) >= maxConnections) {
			return;
		}
		try {
			// System.out.println("connecting one");
			Future<TcpConnection> future = TcpNetFactory.connect(context, endpoint);
			connecting.add(future);
		} catch (IOException e) {
			System.out.println("connect failed: " + ExceptionUtil.buildMessageChain(e));
		}
	}

	private void closeTooOld(long maxAgeMs) {
		long killIfOlder = System.currentTimeMillis() - maxAgeMs;
		Iterator<RichEchoClientSession> iter = sessions.iterator();
		while (iter.hasNext()) {
			RichEchoClientSession recs = iter.next();
			if (recs.createdAt < killIfOlder) {
				iter.remove();
				// System.out.println("closing: " + recs.session);
				close(recs);
			}
		}
	}

	private static class RichEchoClientSession {

		final EchoClientSession session;

		final long createdAt = System.currentTimeMillis();

		private RichEchoClientSession(EchoClientSession session) {
			super();
			this.session = session;
		}
	}
}
