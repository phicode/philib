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
import java.net.InetSocketAddress;
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
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.context.ScalableNetContext;
import ch.bind.philib.net.session.EchoClientSession;
import ch.bind.philib.net.tcp.TcpNetFactory;

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

	private static final boolean DEBUG_MODE = true;

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

	private List<Future<Session>> connecting = new LinkedList<Future<Session>>();

	private long numConnectFails;

	private long numCloseFails;

	private InetSocketAddress endpoint;

	private NetContext context;

	private static final SessionFactory sessionFactory = new SessionFactory() {

		@Override
		public Session createSession(Connection connection) throws IOException {
			EchoClientSession session = new EchoClientSession(connection, VERIFY_MODE);
			session.send();
			return session;
		}
	};

	private void run(int numClients) {
		// endpoint = SocketAddresses.fromIp("10.0.0.71", 1234);
		// endpoint = SocketAddresses.fromIp("10.95.162.221", 1234);
		try {
			endpoint = SocketAddresses.fromIp("127.0.0.1", 1234);
		} catch (UnknownHostException e) {
			System.out.println("unknown host: " + ExceptionUtil.buildMessageChain(e));
			return;
		}

		// context = new SimpleNetContext();
		context = new ScalableNetContext(16);
		// context.setTcpNoDelay(true);
		// context.setSndBufSize(64 * 1024);
		// context.setRcvBufSize(64 * 1024);
		context.setTcpNoDelay(false);
		context.setSndBufSize(512);
		context.setRcvBufSize(512);
		context.setDebugMode(DEBUG_MODE);

		final long printStatsIntervalMs = 10000;
		final long start = System.currentTimeMillis();

		// final long rampUpMs = 100L;
		// final long rampDownMs = 60000L;
		// final int maxConnections = 2000;

		final long rampUpMs = 50L;
		final long rampDownMs = 200000L;
		final int maxConnections = 1000;

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
			System.out.printf("last %dsec total-rx=%.3fM, total-tx=%.3fM bytes => %.5f mbit/sec, #connections: %d, connecting: %d%n", //
					intervalMs / 1000, rxMb, txMb, mbit, num, connecting.size());
		}
		if (sessions.isEmpty() && connecting.isEmpty()) {
			System.out.println("no active or connecting sessions!");
		}
	}

	private void handleConnected() throws InterruptedException {
		Iterator<Future<Session>> iter = connecting.iterator();
		while (iter.hasNext()) {
			Future<Session> fes = iter.next();
			if (fes.isDone()) {
				iter.remove();
				try {
					Session s = fes.get();
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
			Future<Session> future = TcpNetFactory.asyncOpen(context, endpoint, sessionFactory);
			connecting.add(future);
		} catch (IOException e) {
			System.out.println("asyncOpenClient failed: " + ExceptionUtil.buildMessageChain(e));
		}
	}

	private void closeTooOld(long maxAgeMs) {
		long killIfOlder = System.currentTimeMillis() - maxAgeMs;
		Iterator<RichEchoClientSession> iter = sessions.iterator();
		while (iter.hasNext()) {
			RichEchoClientSession recs = iter.next();
			if (recs.createdAt < killIfOlder) {
				iter.remove();
				System.out.println("closing: " + recs.session + ", " + recs.session.getConnection().getDebugInformations());
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
