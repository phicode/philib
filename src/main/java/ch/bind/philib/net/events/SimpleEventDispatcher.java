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

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.lang.ServiceState;
import ch.bind.philib.lang.ThreadUtil;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
// TODO: thread safe
public final class SimpleEventDispatcher implements EventDispatcher {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleEventDispatcher.class);

	private static final AtomicLong NAME_SEQ = new AtomicLong(0);

	private final Queue<NewRegistration> newRegistrations = new ConcurrentLinkedQueue<NewRegistration>();

	private final ServiceState serviceState = new ServiceState();

	private final Selector selector;

	private Thread dispatcherThread;

	private long dispatcherThreadId;

	// TODO: use a long->object map
	private final Map<Long, EventHandler> handlersWithUndeliveredData = new ConcurrentHashMap<Long, EventHandler>();

	private SimpleEventDispatcher(Selector selector) {
		this.selector = selector;
	}

	public static EventDispatcher open() {
		Selector selector;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new SelectorCreationException(e);
		}
		SimpleEventDispatcher disp = new SimpleEventDispatcher(selector);
		String threadName = SimpleEventDispatcher.class.getSimpleName() + '-' + NAME_SEQ.getAndIncrement();
		Thread dispatcherThread = ThreadUtil.createForeverRunner(disp, threadName);
		disp.initDispatcherThreads(dispatcherThread);
		dispatcherThread.start();
		return disp;
	}

	private synchronized void initDispatcherThreads(Thread thread) {
		this.dispatcherThreadId = thread.getId();
		this.dispatcherThread = thread;
	}

	@Override
	public boolean isOpen() {
		return serviceState.isOpen();
	}

	@Override
	public void run() {
		if (serviceState.isUninitialized()) {
			serviceState.setOpen();
		}
		try {
			int selectIoeInArow = 0;
			while (serviceState.isOpen() && selectIoeInArow < 5) {
				// TODO: start to sleep with an exponentianl backoff when select
				// fails ... why can it fail?
				int num;
				try {
					num = select();
					selectIoeInArow = 0;
				} catch (IOException e) {
					selectIoeInArow++;
					LOG.error("Selector.select() threw an IOException: " + ExceptionUtil.buildMessageChain(e));
					continue;
				}
				if (num > 0) {
					Set<SelectionKey> selected = selector.selectedKeys();
					for (SelectionKey key : selected) {
						handleReadyKey(key);
					}
					selected.clear();
				}

				if (!handlersWithUndeliveredData.isEmpty()) {
					// TODO: more efficient traversal
					for (EventHandler eh : handlersWithUndeliveredData.values()) {
						SelectionKey key = eh.getChannel().keyFor(selector);
						handleEvent(eh, key, EventUtil.READ);
					}
				}
			}
		} catch (ClosedSelectorException e) {
			System.out.println("shutting down");
		}
	}

	private int select() throws IOException, ClosedSelectorException {
		int num;
		boolean longSelect = true;
		do {
			updateRegistrations();

			longSelect = handlersWithUndeliveredData.isEmpty();
			if (longSelect) {
				num = selector.select(10000L);
			} else {
				num = selector.selectNow();
			}
		} while (num == 0 && longSelect && serviceState.isOpen());
		return num;
	}

	private void updateRegistrations() {
		NewRegistration reg = null;
		while ((reg = newRegistrations.poll()) != null) {
			EventHandler eventHandler = reg.getEventHandler();
			try {
				SelectableChannel channel = eventHandler.getChannel();
				int ops = reg.getOps();
				channel.register(selector, ops, eventHandler);
			} catch (ClosedChannelException e) {
				SafeCloseUtil.close(eventHandler, LOG);
			}
		}
	}

	@Override
	public void close() {
		Thread t = dispatcherThread;
		if (t != null) {
			// tell the dispatcher to stop processing events
			serviceState.setClosing();
			wakeup();
			ThreadUtil.interruptAndJoin(t);
			dispatcherThread = null;

			for (SelectionKey key : selector.keys()) {
				if (key.isValid()) {
					Object att = key.attachment();
					if (att instanceof EventHandler) {
						EventHandler e = (EventHandler) att;
						SafeCloseUtil.close(e, LOG);
					}
				}
			}

			SafeCloseUtil.close(selector, LOG);

			for (NewRegistration newReg : newRegistrations) {
				SafeCloseUtil.close(newReg.eventHandler, LOG);
			}

			serviceState.setClosed();
		}
	}

	private void handleReadyKey(final SelectionKey key) {
		final EventHandler eventHandler = (EventHandler) key.attachment();
		if (eventHandler == null) {
			// cancelled key
			return;
		}
		if (!key.isValid()) {
			SafeCloseUtil.close(eventHandler, LOG);
		}
		int readyOps = key.readyOps();
		handleEvent(eventHandler, key, readyOps);
	}

	private void handleEvent(final EventHandler handler, final SelectionKey key, final int ops) {
		try {
			int interestedOps = key.interestOps();
			int newInterestedOps = handler.handle(ops);
			if (newInterestedOps != interestedOps) {
				key.interestOps(newInterestedOps);
			}
		} catch (Exception e) {
			// TODO: log as trace?
			LOG.info("closing an event-handler due to an unexpected exception: " + ExceptionUtil.buildMessageChain(e));
			SafeCloseUtil.close(handler, LOG);
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
			// channel is not registered for this selector
			register(eventHandler, ops);
		} else {
			key.interestOps(ops);
			if (asap) {
				wakeup();
			}
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
		}
	}

	@Override
	public void registerForRedeliverPartialReads(EventHandler eventHandler) {
		handlersWithUndeliveredData.put(eventHandler.getEventHandlerId(), eventHandler);
	}

	@Override
	public void unregisterFromRedeliverPartialReads(EventHandler eventHandler) {
		handlersWithUndeliveredData.remove(eventHandler.getEventHandlerId());
	}

	@Override
	public boolean isEventDispatcherThread(final Thread thread) {
		return (thread != null) && (thread.getId() == dispatcherThreadId);
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
}
