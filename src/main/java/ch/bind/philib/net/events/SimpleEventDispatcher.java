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
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
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
public final class SimpleEventDispatcher implements RichEventDispatcher {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleEventDispatcher.class);

	private static final AtomicLong NAME_SEQ = new AtomicLong(0);

	private final Queue<NewRegistration> newRegistrations = new ConcurrentLinkedQueue<NewRegistration>();

	// TODO: use a long->object map
	private final ConcurrentMap<Long, EventHandler> handlersWithUndeliveredData = new ConcurrentHashMap<Long, EventHandler>();

	private final ServiceState serviceState = new ServiceState();

	private final Selector selector;

	private Thread dispatcherThread;

	private long dispatcherThreadId;

	private SimpleEventDispatcher(Selector selector, Thread dispatcherThread) {
		this.selector = selector;
		this.dispatcherThread = dispatcherThread;
		this.dispatcherThreadId = dispatcherThread.getId();
		//TODO
//		serviceState.setOpen();
	}

	public static RichEventDispatcher open() {
		Selector selector;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new SelectorCreationException(e);
		}
		SimpleEventDispatcher disp = new SimpleEventDispatcher(selector);
		String threadName = SimpleEventDispatcher.class.getSimpleName() + '-' + NAME_SEQ.getAndIncrement();
		Thread dispatcherThread = ThreadUtil.createAndStartForeverRunner(disp, threadName);
		disp.initDispatcherThreads(dispatcherThread);
		return disp;
	}

	@Override
	public long getDispatcherThreadId() {
		return dispatcherThreadId;
	}

	@Override
	public boolean isOpen() {
		return serviceState.isOpen();
	}

	@Override
	public int getNumEventHandlers() {
		// this is actually not thread safe but we access the information in read-only mode
		return selector.keys().size();
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
		if (key.isValid()) {
			handleEvent(eventHandler, key, key.readyOps());
		} else {
			SafeCloseUtil.close(eventHandler, LOG);
		}
	}

	private void handleEvent(final EventHandler handler, final SelectionKey key, final int ops) {
		try {
			int interestedOps = key.interestOps();
			int newInterestedOps = handler.handle(ops);
			if (newInterestedOps != EventUtil.OP_DONT_CHANGE && newInterestedOps != interestedOps) {
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
			key.attach(eventHandler);
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
		} else {
			removePendingRegistration(eventHandler);
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
	
	private void removePendingRegistration(EventHandler eventHandler) {
		int id = eventHandler.getEventHandlerId();
		Iter
		for (NewRegistration newReg:  newRegistrations )
	}

	private static final class EventDispatchWorker implements Runnable {

		@Override
		public void run() {
			if (serviceState.isUninitialized()) {
				serviceState.setOpen();
			}
			try {
				int selectIoeInArow = 0;
				while (serviceState.isOpen() && selectIoeInArow < 5) {
					// TODO: start to sleep with an exponential backoff when select
					// fails ... why can it fail anyway?
					int num;
					try {
						num = select();
						selectIoeInArow = 0;
					} catch (IOException e) {
						selectIoeInArow++;
						LOG.error("IOException in Selector.select(): " + ExceptionUtil.buildMessageChain(e));
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
				serviceState.setClosed();
			}
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
}
