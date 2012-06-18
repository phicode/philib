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
package ch.bind.philib.net.events;

import static ch.bind.philib.io.BitOps.checkMask;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.lang.ThreadUtil;

// TODO: thread safe
public final class SimpleEventDispatcher implements EventDispatcher {

	private static final AtomicLong NAME_SEQ = new AtomicLong(0);

	private static final boolean doSelectTimings = false;

	private final Queue<NewRegistration> newRegistrations = new ConcurrentLinkedQueue<NewRegistration>();

	private final Selector selector;

	private Thread dispatcherThread;

	private long dispatcherThreadId;

	private SimpleEventDispatcher(Selector selector) throws IOException {
		this.selector = selector;
	}

	public static EventDispatcher open() throws IOException {
		Selector selector = Selector.open();
		try {
			SimpleEventDispatcher rv = new SimpleEventDispatcher(selector);
			String threadName = SimpleEventDispatcher.class.getSimpleName() + '-' + NAME_SEQ.getAndIncrement();
			Thread dispatcherThread = ThreadUtil.runForever(rv, threadName);
			rv.initDispatcherThreads(dispatcherThread);
			return rv;
		} catch (IOException e) {
			try {
				selector.close();
			} catch (Exception e2) {
				// TODO: logging
				System.err.println("failed to close an unused selector: " + e2.getMessage());
				e2.printStackTrace(System.err);
			}
			throw new IOException("failed to create an event dispatcher", e);
		}
	}

	private synchronized void initDispatcherThreads(Thread dispatcherThread) {
		this.dispatcherThreadId = dispatcherThread.getId();
		this.dispatcherThread = dispatcherThread;
	}

	@Override
	public void run() {
		int lastKeys = 0;
		try {
			while (true) {
				int num = select();
				if (num > 0) {
					Set<SelectionKey> selected = selector.selectedKeys();
					for (SelectionKey key : selected) {
						handleReadyKey(key);
					}
					selected.clear();
				}
				int keys = selector.keys().size();
				if (keys != lastKeys) {
					System.out.printf("keys now=%d, last=%d%n", keys, lastKeys);
					lastKeys = keys;
				}
			}
		} catch (ClosedSelectorException e) {
			System.out.println("shutting down");
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private int select() throws IOException, ClosedSelectorException {
		int num;
		do {
			updateRegistrations();

			if (doSelectTimings) {
				long selStart = System.nanoTime();
				num = selector.select(10000L);
				long selEnd = System.nanoTime();
				long selTime = selEnd - selStart;
				long selMs = selTime / (10L * 1000L * 1000L);
				if (selMs >= 10005) {
					System.out.printf("select took %dms, num=%d%n", selMs, num);
				}
			}
			else {
				num = selector.select(10000L);
			}
		} while (num == 0);
		return num;
	}

	private void updateRegistrations() {
		NewRegistration reg = newRegistrations.poll();
		while (reg != null) {
			EventHandler eventHandler = reg.getEventHandler();
			SelectableChannel channel = eventHandler.getChannel();
			int ops = reg.getOps();
			try {
				long ts = System.nanoTime();
				channel.register(selector, ops, eventHandler);
				long te = System.nanoTime();
				long t = te - ts;
				System.out.printf("register took: %dns => %.5fms%n", t, (t / 1000000f));
			} catch (ClosedChannelException e) {
				System.out.println("cant register an already closed channel");
			}

			reg = newRegistrations.poll();
		}
	}

	@Override
	public void close() {
		Thread t = dispatcherThread;
		if (t != null) {
			dispatcherThread = null;
			ThreadUtil.interruptAndJoin(t);
			try {
				selector.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (SelectionKey key : selector.keys()) {
				if (key.isValid()) {
					Object att = key.attachment();
					if (att instanceof EventHandler) {
						EventHandler e = (EventHandler) att;
						try {
							e.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
			for (NewRegistration newReg : newRegistrations) {
				try {
					newReg.eventHandler.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void handleReadyKey(final SelectionKey key) {
		EventHandler eventHandler = (EventHandler) key.attachment();
		if (eventHandler == null) {
			// canceled key
			return;
		}
		if (!key.isValid()) {
			closeHandler(eventHandler);
		}
		int readyOps = key.readyOps();
		try {
			if (checkMask(readyOps, EventUtil.READ)) {
				eventHandler.handleRead();
			}
			if (checkMask(readyOps, EventUtil.WRITE)) {
				eventHandler.handleWrite();
			}
			if (checkMask(readyOps, EventUtil.ACCEPT)) {
				eventHandler.handleAccept();
			}
			if (checkMask(readyOps, EventUtil.CONNECT)) {
				eventHandler.handleConnect();
			}
		} catch (Exception e) {
			System.err.println("eventHandler.handle*() failed, closing: " + ExceptionUtil.buildMessageChain(e));
			e.printStackTrace(System.err);
			closeHandler(eventHandler);
		}
	}

	private void closeHandler(EventHandler eventHandler) {
		try {
			eventHandler.close();
		} catch (Exception e) {
			// TODO
			System.err.println("exception while closing: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void register(EventHandler eventHandler, int ops) {
		newRegistrations.add(new NewRegistration(eventHandler, ops));
		wakeup();
	}

	private void wakeup() {
		Thread t = dispatcherThread;
		if (t != null && t != Thread.currentThread()) {
			selector.wakeup();
		}
	}

	@Override
	public void reRegister(EventHandler eventHandler, int ops, boolean asap) {
		SelectableChannel channel = eventHandler.getChannel();
		SelectionKey key = channel.keyFor(selector);
		if (key == null) {
			System.out.println("!!!!!!!!!!!!!!! channel is not registered for this selector");
		}
		else {
			key.interestOps(ops);
		}
		if (asap) {
			wakeup();
		}
	}

	@Override
	public void unregister(EventHandler eventHandler) {
		SelectableChannel channel = eventHandler.getChannel();
		SelectionKey key = channel.keyFor(selector);
		if (key != null) {
			key.cancel();
			key.attach(null);
			wakeup();
			System.out.println("unreg, keys: " + selector.keys().size());
		}
		else {
			System.out.println("unreg failed, not registered");
		}
	}

	private static final class NewRegistration {

		final EventHandler eventHandler;

		final int ops;

		private NewRegistration(EventHandler eventHandler, int ops) {
			this.eventHandler = eventHandler;
			this.ops = ops;
		}

		public EventHandler getEventHandler() {
			return eventHandler;
		}

		public int getOps() {
			return ops;
		}
	}

	@Override
	public boolean isEventDispatcherThread(Thread thread) {
		return (thread != null) && (thread.getId() == dispatcherThreadId);
	}
}
