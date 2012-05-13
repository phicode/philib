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
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import ch.bind.philib.net.BaseSession;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.tcp.TcpConnection;

//TODO: reply data validation
//TODO: speed measurements
//TODO: many threads
public class TcpEchoClient extends BaseSession {

	// private byte[] buf;

	private TcpConnection connection;

	// private AtomicInteger expectInput = new AtomicInteger();

	// private AtomicLong counter = new AtomicLong();

	// private long start;

	// private long nextBlubber;

	private BlockingQueue<byte[]> inbox = new LinkedBlockingQueue<byte[]>();

	public static void main(String[] args) throws Exception {
		new TcpEchoClient().run();
	}

	private void run() throws IOException, InterruptedException {
		InetSocketAddress endpoint = SocketAddresses.fromIp("127.0.0.1", 1234);
		connection = TcpConnection.open(endpoint, this);

		// buf = new byte[8 * 1024];
		byte[] buf = new byte[128 * 1024];
		new Random().nextBytes(buf);

		long start = System.currentTimeMillis();
		// NQueue<byte[]> out = connection.getOut();
		// NQueue<byte[]> in = connection.getIn();
		long total = 0;
		long nextStatus = 0;
		while (true) {
			int num = connection.send(buf);
			System.out.printf("sent: %d / %d%n", num, buf.length);
			long remaining = num;
			do {
				// dataSem.acquire();
				byte[] recv = inbox.take();
				if (recv != null) {
					remaining -= recv.length;
				}
				else {
					System.out.println("empty poll returned");
				}
			} while (remaining > 0);
			total += buf.length;

			long now = System.currentTimeMillis();
			if (now > nextStatus) {
				nextStatus = now + 1000;
				long t = now - start;
				double mbPerSec = (total / (1024f * 1024f)) / (t / 1000f);
				System.out.printf("%d * 2 bytes in %d ms => %.3f * 2 mb/sec%n", total, t, mbPerSec);
			}
		}
	}

	//
	// private void send() throws IOException {
	// // try {
	// // Thread.sleep(5000);
	// // } catch (InterruptedException e) {
	// // e.printStackTrace();
	// // }
	// expectInput.addAndGet(buf.length);
	// connection.send(buf);
	// counter.addAndGet(buf.length);
	// long now = System.currentTimeMillis();
	// if (now > nextBlubber) {
	// nextBlubber = now + 1000;
	// long t = now - start;
	// double mbPerSec = (counter.get() / (1024f * 1024f)) / (t / 1000f);
	// System.out.printf("%d bytes in %d ms => %.3f mb/sec%n", counter.get(), t,
	// mbPerSec);
	// }
	// }

	@Override
	public void receive(byte[] data) {
		inbox.add(data);
	}

	@Override
	public void closed() {
		// TODO Auto-generated method stub

	}

	// @Override
	// public void receive(byte[] data) throws IOException {
	// expectInput.addAndGet(-data.length);
	// int missing = expectInput.get();
	// if (missing < 0) {
	// String msg = "server sent back more data then we sent, WTF? " + missing;
	// System.out.println(msg);
	// throw new Error(msg);
	// }
	// else if (missing == 0) {
	// // System.out.println("server replied, sending question again");
	// send();
	// }
	// else {
	// // System.out.println("received data, but still missing: " +
	// // missing);
	// }
	// }

	// @Override
	// public void closed() {
	// System.out.println("connection closed");
	// }
}
