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
import java.nio.ByteBuffer;
import java.util.Random;

import ch.bind.philib.lang.ThreadUtil;
import ch.bind.philib.net.PureSessionBase;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.tcp.TcpConnection;

//TODO: reply data validation
//TODO: speed measurements
//TODO: many threads
public class TcpEchoClient {

	private TcpConnection connection;

	public static void main(String[] args) throws Exception {
		new TcpEchoClient().run();
	}

	private void run() throws IOException, InterruptedException {
		// InetSocketAddress endpoint = SocketAddresses.fromIp("10.0.0.66",
		// 1234);
		// InetSocketAddress endpoint = SocketAddresses.fromIp("10.95.162.221",
		// 1234);

		byte[] buf = new byte[8 * 1024];
		new Random().nextBytes(buf);
		ByteBuffer seedBuffer = ByteBuffer.wrap(buf);

		InetSocketAddress endpoint = SocketAddresses.fromIp("127.0.0.1", 1234);
		EchoSession session = new EchoSession();
		connection = TcpConnection.open(endpoint, session);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		long lastT = System.currentTimeMillis();
		connection.sendBlocking(seedBuffer);
		long seeded = seedBuffer.capacity();
		int loop = 1;
		long lastRx = 0, lastTx = 0;
		final long start = System.currentTimeMillis();
		while (connection.isConnected()) {
			long sleepUntil = start + (loop * 30000L);
			ThreadUtil.sleepUntilMs(sleepUntil);

			long rx = session.getRx();
			long tx = session.getTx();
			long rxDiff = rx - lastRx;
			long txDiff = rx - lastTx;
			long now = System.currentTimeMillis();
			long tDiff = now - lastT;
			double rxMbPerSec = (rxDiff / (1024f * 1024f)) / (tDiff / 1000f);
			double txMbPerSec = (txDiff / (1024f * 1024f)) / (tDiff / 1000f);
			System.out.printf("seeded=%d total=%d in %d ms; last 5sec rx=%d, tx=%d bytes => %.3f %.3f mb/sec%n", //
					seeded, (rx + tx), (now - start), rxDiff, txDiff, rxMbPerSec, txMbPerSec);
			if (seeded < 128 * 1024) {
				System.out.println("seeding an additional " + seedBuffer.capacity() + " bytes into the echo chain");
				seedBuffer.rewind();
				connection.sendBlocking(seedBuffer);
				seeded += seedBuffer.capacity();
			}
			// if (rxDiff == 0 || txDiff == 0) {
			session.forceWrite();
			// }
			loop++;
			lastRx = rx;
			lastTx = tx;
			lastT = now;
		}
	}
}
