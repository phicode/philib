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
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.net.BaseSession;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.tcp.TcpConnection;

//TODO: reply data validation
//TODO: speed measurements
//TODO: many threads
public class TcpEchoClient extends BaseSession {

	private TcpConnection connection;

	private final AtomicLong tx = new AtomicLong(0);

	private final AtomicLong rx = new AtomicLong(0);

	public static void main(String[] args) throws Exception {
		new TcpEchoClient().run();
	}

	private void run() throws IOException, InterruptedException {
		InetSocketAddress endpoint = SocketAddresses.fromIp("127.0.0.1", 1234);
		connection = TcpConnection.open(endpoint, this);

		// buf = new byte[8 * 1024];
		byte[] buf = new byte[128 * 1024];
		new Random().nextBytes(buf);

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

		long start = System.currentTimeMillis();
		long nextStatus = 0;
		int num = connection.send(buf);
		if (num != buf.length) {
			System.out.printf("sent: %d / %d%n", num, buf.length);
		}
		tx.addAndGet(num);
		while (true) {

			long now = System.currentTimeMillis();
			if (now > nextStatus) {
				nextStatus = now + 1000;
				long t = now - start;
				long rx = this.rx.get();
				long tx = this.tx.get();
				double rxMbPerSec = (rx / (1024f * 1024f)) / (t / 1000f);
				double txMbPerSec = (tx / (1024f * 1024f)) / (t / 1000f);
				System.out.printf("rx=%d, tx=%d bytes in %d ms => %.3f %.3f mb/sec%n", rx, tx, t, rxMbPerSec, txMbPerSec);
			}
		}
	}

	@Override
	public void receive(byte[] data) {
		rx.addAndGet(data.length);
		try {
			send(data);
			tx.addAndGet(data.length);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void closed() {
		// TODO Auto-generated method stub
		System.out.println("closed!");
	}
}
