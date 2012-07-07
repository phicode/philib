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

import ch.bind.philib.lang.ThreadUtil;
import ch.bind.philib.net.Connection;
import ch.bind.philib.net.Session;
import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.context.NetContext;
import ch.bind.philib.net.context.SimpleNetContext;
import ch.bind.philib.net.tcp.TcpNetFactory;

// TODO: reply data validation
// TODO: latency measurements
// TODO: many threads
public class TcpEchoClient {

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

	private void run(int numClients) throws IOException, InterruptedException {
		// InetSocketAddress endpoint = SocketAddresses.fromIp("10.0.0.71", 1234);
		// InetSocketAddress endpoint = SocketAddresses.fromIp("10.95.162.221", 1234);
		InetSocketAddress endpoint = SocketAddresses.fromIp("127.0.0.1", 1234);

		int numRunning = 0;
		long rampUpMs = 1000;
		long startNext = System.currentTimeMillis();

		// byte[] buf = new byte[8 * 1024];
		// new Random().nextBytes(buf);
		// ByteBuffer seedBuffer = ByteBuffer.wrap(buf);
		NetContext context = new SimpleNetContext();
		context.setTcpNoDelay(true);
		context.setSndBufSize(64 * 1024);
		context.setRcvBufSize(64 * 1024);
		SessionFactory factory = new SessionFactory() {
			@Override
			public Session createSession(Connection connection) {
				return new EchoSession(connection, false	, true);
			}
		};
		EchoSession session = (EchoSession) TcpNetFactory.INSTANCE.openClient(context, endpoint, factory);

		final int loopTimeSec = 10;
		long lastT = System.currentTimeMillis();
		// connection.sendSync(seedBuffer);
		// long seeded = seedBuffer.capacity();
		int loop = 1;
		long lastRx = 0, lastTx = 0;
		final long start = System.currentTimeMillis();
		session.incInTransitBytes(8192);
		int seeded = 8192;
		session.send();
		while (session.connection.isConnected()) {
			long sleepUntil = start + (loop * loopTimeSec * 1000L);
			ThreadUtil.sleepUntilMs(sleepUntil);

			long rx = session.connection.getRx();
			long tx = session.connection.getTx();
			long rxDiff = rx - lastRx;
			long txDiff = tx - lastTx;
			long diff = rxDiff + txDiff;
			long now = System.currentTimeMillis();
			long tDiff = now - lastT;
			double mbit = (diff * 8) / 1e6 / (tDiff / 1000f);
			double rxMb = rxDiff / ((double) (1024 * 1024f));
			double txMb = txDiff / ((double) (1024 * 1024f));
			System.out.printf("seed=%d, last %dsec rx=%.3fM, tx=%.3fM bytes => %.5f mbit/sec rxTx=%d tDiff=%d%n", //
					seeded, loopTimeSec, rxMb, txMb, mbit, (rxDiff + txDiff), tDiff);
			if (seeded < 512 * 1024) {
				// System.out.println("seeding an additional " + 8192 +
				// " bytes into the echo chain");
				session.incInTransitBytes(8192);
				seeded += 8192;
			}
			loop++;
			lastRx = rx;
			lastTx = tx;
			lastT = now;
		}
		// TODO: save shutdown of clients
		System.exit(0);
	}
}
