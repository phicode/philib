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
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ch.bind.philib.net.Consumer;
import ch.bind.philib.net.ConsumerFactory;
import ch.bind.philib.net.NetSelector;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.validation.SimpleValidation;

public class TcpServer implements NetServer {

	// TODO: configurable
	private static final int DEFAULT_BACKLOG = 100;

	private NetSelector selector;

	private ServerSocketChannel channel;

	private final ConsumerFactory consumerFactory;

	TcpServer(ConsumerFactory consumerFactory) {
		SimpleValidation.notNull(consumerFactory);
		this.consumerFactory = consumerFactory;
	}

	// TODO: open(SocketAddress) with default netselector
	void open(NetSelector selector, SocketAddress bindAddress) throws IOException {
		this.selector = selector;

		ServerSocketChannel channel = ServerSocketChannel.open();
		ServerSocket socket = channel.socket();
		socket.bind(bindAddress, DEFAULT_BACKLOG);
		channel.configureBlocking(false);
		// TODO: log bridge
		System.out.println("listening on: " + bindAddress);
		// int ops = channel.validOps();
		// System.out.println("validOps = " + ops + //
		// " write: " + (ops & SelectionKey.OP_WRITE) + //
		// " read: " + (ops & SelectionKey.OP_READ) + //
		// " connect: " + (ops & SelectionKey.OP_CONNECT) + //
		// " accept: " + (ops & SelectionKey.OP_ACCEPT));
		this.channel = channel;
		selector.register(this);
	}

	@Override
	public SelectableChannel getChannel() {
		// TODO: validate open
		return channel;
	}

	@Override
	public void close() throws IOException {
		// TODO: client connections
		selector.unregister(this);
		channel.close();
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getSelectorOps() {
		return SelectionKey.OP_ACCEPT;
	}

	@Override
	public boolean handle(int selectOp) {
		if (selectOp == SelectionKey.OP_ACCEPT) {
			System.out.println("doAccept");
			doAccept();
			return false;
		} else {
			throw new IllegalArgumentException("illegal select-op");
		}
	}
	
	@Override
	public void closed() {
		// TODO Auto-generated method stub
//	consumer.closed();	
		throw new IllegalStateException();
	}

	private void doAccept() {
		try {
			SocketChannel clientChannel = channel.accept();
			Consumer consumer = consumerFactory.createConsumer();
			TcpConnection connection = new TcpConnection(clientChannel, consumer, selector);
			consumer.init(connection);
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}

	@Override
	public int getActiveSessionCount() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	// private static class Handler implements Runnable {
	// private final SocketChannel clientChannel;
	//
	// public Handler(SocketChannel clientChannel) throws IOException {
	// super();
	// this.clientChannel = clientChannel;
	// clientChannel.configureBlocking(false);
	// System.out.println("send-buffer-size: " +
	// clientChannel.socket().getSendBufferSize());
	// System.out.println("recv-buffer-size: " +
	// clientChannel.socket().getReceiveBufferSize());
	// System.out.println("tcp-no-delay    : " +
	// clientChannel.socket().getTcpNoDelay());
	// clientChannel.socket().setTcpNoDelay(true);
	// }
	//
	// @Override
	// public void run() {
	// // ByteBuffer buffer = ByteBuffer.allocate(4096);
	// // RingBuffer receiveBuffer = new RingBuffer(16 * 1024);
	//
	// ByteBuffer buffer = ByteBuffer.allocate(128);
	// RingBuffer receiveBuffer = new RingBuffer(512);
	//
	// final int MAX_IN_BUF = receiveBuffer.capacity() - buffer.capacity();
	// // byte[] reader = new byte[1024];
	// final int rwOps = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
	// try {
	// long lastWrite = 0;
	// Selector sel = Selector.open();
	// SelectionKey key = clientChannel.register(sel, rwOps);
	// long total = 0;
	// boolean printInputBufferFullIfFull = true;
	// while (true) {
	// int num = sel.select();
	// if (num < 1) {
	// continue;
	// }
	// Set<SelectionKey> keys = sel.selectedKeys();
	// if (!keys.contains(key)) {
	// System.out.println("huh????");
	// continue;
	// }
	// keys.remove(key);
	//
	// boolean canRead = key.isReadable();
	// boolean canWrite = key.isWritable();
	// // System.out.println("readable: " + canRead);
	// // System.out.println("writable: " + canWrite);
	// if (key.interestOps() != rwOps) {
	// throw new IllegalStateException();
	// }
	//
	// // read phase
	// if (canRead) {
	// if (receiveBuffer.available() < MAX_IN_BUF) {
	// buffer.clear();
	// final int len = clientChannel.read(buffer);
	// if (len == -1) {
	// System.out.println("connection closed");
	// return;
	// } else {
	// System.out.println("read: " + len);
	// }
	// buffer.flip();
	// receiveBuffer.write(buffer.array(), buffer.arrayOffset(), len);
	// printInputBufferFullIfFull = true;
	// } else {
	// if (printInputBufferFullIfFull) {
	// System.out.println("input buffer full");
	// }
	// printInputBufferFullIfFull = false;
	// }
	// // if (lastWrite != 0 && (lastWrite + 1000) <
	// // System.currentTimeMillis()) {
	// // System.out.println("discarding input buffer");
	// // boolean cont = false;
	// // long discarded = 0;
	// // do {
	// // int len = clientChannel.read(buffer);
	// // cont = len > 0;
	// // discarded += len;
	// // } while (cont);
	// // System.out.println("discarded " + discarded +
	// // " input buffer bytes");
	// // }
	// // int rem = len;
	// // while (rem > 0) {
	// // int toRead = Math.min(rem, reader.length);
	// // buffer.get(reader, 0, toRead);
	// // rem -= toRead;
	// // }
	// }
	//
	// // write phase
	// long totalWrite = 0;
	// if (receiveBuffer.available() > 0) {
	// // System.out.println("try write");
	// while (true) {
	// buffer.clear();
	// int len = Math.min(receiveBuffer.available(), buffer.capacity());
	// receiveBuffer.read(buffer.array(), buffer.arrayOffset(), len);
	// buffer.position(0);
	// buffer.limit(len);
	// if (buffer.remaining() != len) {
	// throw new AssertionError();
	// }
	// int written = clientChannel.write(buffer);
	// if (written > 0) {
	// if (buffer.remaining() + written != len) {
	// throw new AssertionError();
	// }
	// totalWrite += written;
	// lastWrite = System.currentTimeMillis();
	// } else {
	// receiveBuffer.writeFront(buffer.array(), buffer.arrayOffset() +
	// buffer.position(), buffer.remaining());
	// break;
	// }
	// System.out.println("written: " + written);
	// }// while (fullWrite && receiveBuffer.available() > 0);
	// }
	// // System.out.println("totalWrite: " + totalWrite);
	// // else {
	// // System.out.println("writable, but no data");
	// // }
	// if (!canWrite && totalWrite > 0) {
	// System.out.println("!canWrite && totalWrite == " + totalWrite);
	// }
	// // for non-echo usage:
	// // buffer.clear(),
	// // buffer.write(),
	// // buffer.flip()
	// // buffer.rewind();
	// // // debug("rewind", buffer);
	// // int remaining = buffer.remaining();
	// // int blocked = 0;
	// // do {
	// //
	// // if (written != remaining) {
	// // blocked++;
	// // // System.out.println("output buffer full, wrote " +
	// // // written + " out of " + remaining);
	// // } else {
	// // // System.out.println("good write");
	// // }
	// // remaining = buffer.remaining();
	// // } while (remaining > 0);
	// //
	// // total += len;
	// // System.out.println("blocked: " + blocked);
	// }
	// } catch (IOException e) {
	// System.out.println("io-exc: " + e.getMessage());
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// private static void debug(String op, ByteBuffer buffer) {
	// System.out.println("op: " + op);
	// System.out.println("    limit: " + buffer.limit());
	// System.out.println("    position: " + buffer.position());
	// System.out.println("    capacity: " + buffer.capacity());
	// }
	//
	// private static class Handler2 implements Runnable {
	// private final SocketChannel clientChannel;
	//
	// public Handler2(SocketChannel clientChannel) throws IOException {
	// super();
	// this.clientChannel = clientChannel;
	// clientChannel.configureBlocking(true);
	// System.out.println("send-buffer-size: " +
	// clientChannel.socket().getSendBufferSize());
	// System.out.println("recv-buffer-size: " +
	// clientChannel.socket().getReceiveBufferSize());
	// System.out.println("tcp-no-delay    : " +
	// clientChannel.socket().getTcpNoDelay());
	// clientChannel.socket().setTcpNoDelay(true);
	// clientChannel.socket().setReceiveBufferSize(32 * 1024);
	// clientChannel.socket().setSendBufferSize(32 * 1024);
	// }
	//
	// @Override
	// public void run() {
	// byte[] buffer = new byte[16 * 1024];
	// long total = 0;
	// long lastPrintTime = 0;
	// long start = System.currentTimeMillis();
	// try {
	// InputStream in = clientChannel.socket().getInputStream();
	// OutputStream out = clientChannel.socket().getOutputStream();
	// while (true) {
	// int len = in.read(buffer);
	// if (len == -1) {
	// System.out.println("connection closed");
	// return;
	// } else {
	// out.write(buffer, 0, len);
	// out.flush();
	// total += len;
	// }
	// long now = System.currentTimeMillis();
	// if (now > (lastPrintTime + 1000)) {
	// lastPrintTime = now;
	// double mbPerSec = (total / (1024 * 1024)) / ((now - start) / 1000f);
	// System.out.printf("echoed %d bytes => %.3f mb/sec%n", total, mbPerSec);
	// }
	// }
	// } catch (IOException e) {
	// System.out.println("io-exc: " + e.getMessage());
	// e.printStackTrace();
	// }
	// }
	// }
}
