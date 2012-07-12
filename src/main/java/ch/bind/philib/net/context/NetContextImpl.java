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

import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import ch.bind.philib.cache.ByteBufferCache;
import ch.bind.philib.net.events.EventDispatcher;
import ch.bind.philib.validation.Validation;

public class NetContextImpl implements NetContext {

	public static final int DEFAULT_BUFFER_SIZE = 8192;

	public static final int DEFAULT_NUM_BUFFERS = 128;

	private final ByteBufferCache bufferCache;

	private final EventDispatcher eventDispatcher;

	private Boolean tcpNoDelay;

	private Integer sndBufSize;

	private Integer rcvBufSize;

	private Boolean broadcastDatagram; 
	
	private boolean debugMode;

	public NetContextImpl(ByteBufferCache bufferCache, EventDispatcher eventDispatcher) {
		Validation.notNull(bufferCache);
		Validation.notNull(eventDispatcher);
		this.bufferCache = bufferCache;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public final ByteBufferCache getBufferCache() {
		return bufferCache;
	}

	@Override
	public final EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	@Override
	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}

	@Override
	public Boolean getTcpNoDelay() {
		return tcpNoDelay;
	}

	@Override
	public void setSndBufSize(int size) {
		this.sndBufSize = size;
	}

	@Override
	public Integer getSndBufSize() {
		return sndBufSize;
	}

	@Override
	public void setRcvBufSize(int size) {
		this.rcvBufSize = size;
	}

	@Override
	public Integer getRcvBufSize() {
		return rcvBufSize;
	}

	@Override
	public Boolean isBroadcastDatagram() {
		return broadcastDatagram;
	}
	
	@Override
	public void setBroadcastDatagram(boolean broadcastDatagram) {
		this.broadcastDatagram = broadcastDatagram;
	}
	
	@Override
	public void setSocketOptions(Socket socket) throws SocketException {
		Validation.notNull(socket);
		if (tcpNoDelay != null) {
			socket.setTcpNoDelay(tcpNoDelay);
		}
		if (sndBufSize != null) {
			socket.setSendBufferSize(sndBufSize);
		}
		if (rcvBufSize != null) {
			socket.setReceiveBufferSize(rcvBufSize);
		}
	}

	@Override
	public void setSocketOptions(ServerSocket socket) throws SocketException {
		Validation.notNull(socket);
		if (rcvBufSize != null) {
			socket.setReceiveBufferSize(rcvBufSize);
		}
	}

	@Override
	public void setSocketOptions(DatagramSocket socket) {
		if (broadcastDatagram != null) {
			socket.setBroadcast(broadcastDatagram);
		}
		if (sndBufSize != null) {
			socket.setSendBufferSize(rcvBufSize);
		}
		if (rcvBufSize != null) {
			socket.setReceiveBufferSize(sndBufSize);
		}
	}
	
	@Override
	public boolean isDebugMode() {
		return debugMode;
	}

	@Override
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	
}
