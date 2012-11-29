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
package ch.bind.philib.net.context;

import java.io.Closeable;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import ch.bind.philib.net.events.EventDispatcher;
import ch.bind.philib.pool.buffer.ByteBufferPool;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
// TODO: socket keep-alive options and others
public interface NetContext extends Closeable {

	ByteBufferPool getBufferCache();

	EventDispatcher getEventDispatcher();

	/**
	 * @return {@code null} if not set.
	 */
	Boolean getTcpNoDelay();

	void setTcpNoDelay(boolean tcpNoDelay);

	/**
	 * @return {@code null} if not set.
	 */
	Integer getSndBufSize();

	void setSndBufSize(int size);

	/**
	 * @return {@code null} if not set.
	 */
	Integer getRcvBufSize();

	void setRcvBufSize(int size);

	void setBroadcastDatagram(boolean broadcastDatagram);

	Boolean isBroadcastDatagram();

	int getTcpServerSocketBacklog();

	void getTcpServerSocketBacklog(int tcpServerSocketBacklog);

	void setSocketOptions(Socket socket) throws SocketException;

	void setSocketOptions(ServerSocket socket) throws SocketException;

	void setSocketOptions(DatagramSocket socket) throws SocketException;

	boolean isDebugMode();

	void setDebugMode(boolean debugMode);

	boolean isOpen();
}
