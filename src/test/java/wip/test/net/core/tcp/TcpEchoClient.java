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
package wip.test.net.core.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import wip.src.net.core.Connection;
import wip.src.net.core.Session;
import wip.src.net.core.SessionManager;
import wip.src.net.core.SocketAddresses;
import wip.src.net.core.context.NetContext;
import wip.src.net.core.context.NetContextImpl;
import wip.src.net.core.events.ConcurrentEventDispatcher;
import wip.src.net.core.events.EventDispatcher;
import wip.src.net.core.session.EchoClientSession;
import wip.src.net.core.tcp.TcpConnection;
import wip.src.net.core.tcp.TcpNetFactory;
import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ExceptionUtil;
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
		} else if (args.length == 1) {
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

	private List<RichEchoClientSession> connected = new LinkedList<RichEchoClientSession>();

	private List<RichEchoClientSession> shutdown = new LinkedList<RichEchoClientSession>();

	private List<Future<TcpConnection>> connecting = new LinkedList<Future<TcpConnection>>();

	private long numConnectFails;

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
		context.setConnectTimeout(CONNECT_TIMEOUT);
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

				shutdownOld(rampDownMs);

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

				Thread.sleep((rampUpMs / 2) - 10);
			}
		} catch (InterruptedException e) {

		}
		SafeCloseUtil.close(context);
		System.exit(0);
	}

	private void printStats(long intervalMs, long timeDiff) {
		long totalRx = 0;
		long totalTx = 0;
		int num = connected.size();
		Iterator<RichEchoClientSession> iter = connected.iterator();
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
		if (connected.isEmpty() && connecting.isEmpty() && shutdown.isEmpty()) {
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
					connected.add(new RichEchoClientSession((EchoClientSession) s));
				} catch (ExecutionException e) {
					numConnectFails++;
					System.out.println("exception while opening a connection async: " + ExceptionUtil.buildMessageChain(e));
				}
			}
		}
	}

	private void maybeConnectOne(int maxConnections) {
		int n = connecting.size() + connected.size() + shutdown.size();
		if (n >= maxConnections) {
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

	private void shutdownOld(long maxAgeMs) {
		long shutdownIfOlder = System.currentTimeMillis() - maxAgeMs;
		Iterator<RichEchoClientSession> iter = connected.iterator();
		while (iter.hasNext()) {
			RichEchoClientSession recs = iter.next();
			if (recs.createdAt < shutdownIfOlder) {
				iter.remove();
				shutdown.add(recs);
				recs.session.shutdown();
			} else if (recs.session.isClosed()) {
				iter.remove();
			}
		}

		iter = shutdown.iterator();
		while (iter.hasNext()) {
			RichEchoClientSession recs = iter.next();
			if (recs.session.isClosed()) {
				iter.remove();
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
