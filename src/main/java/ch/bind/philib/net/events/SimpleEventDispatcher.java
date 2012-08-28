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
import java.util.Iterator;
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
import ch.bind.philib.util.LoadAvg;
import ch.bind.philib.util.NoOpLoadAvg;
import ch.bind.philib.util.SimpleLoadAvg;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
// TODO: thread safe
public final class SimpleEventDispatcher implements RichEventDispatcher, Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleEventDispatcher.class);

	private static final AtomicLong NAME_SEQ = new AtomicLong(0);

	private static final int LOAD_AVG_SECONDS = 60;

	private final Queue<NewRegistration> newRegistrations = new ConcurrentLinkedQueue<>();

	// TODO: use a long->object map
	private final ConcurrentMap<Long, EventHandler> handlersWithUndeliveredData = new ConcurrentHashMap<>();

	private final ServiceState serviceState = new ServiceState();

	private final Selector selector;

	private final LoadAvg loadAvg;

	private Thread dispatchThread;

	private long dispatchThreadId;

	private SimpleEventDispatcher(Selector selector, LoadAvg loadAvg) {
		this.selector = selector;
		this.loadAvg = loadAvg;
	}

	public static SimpleEventDispatcher open(boolean collectLoadAverage) {
		Selector selector;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new SelectorCreationException(e);
		}
		LoadAvg loadAvg = collectLoadAverage ? SimpleLoadAvg.forSeconds(LOAD_AVG_SECONDS) : NoOpLoadAvg.INSTANCE;
		SimpleEventDispatcher dispatcher = new SimpleEventDispatcher(selector, loadAvg);
		String threadName = SimpleEventDispatcher.class.getSimpleName() + '-' + NAME_SEQ.getAndIncrement();
		Thread dispatchThread = ThreadUtil.createAndStartForeverRunner(dispatcher, threadName);
		dispatcher.dispatchThread = dispatchThread;
		dispatcher.dispatchThreadId = dispatchThread.getId();
		return dispatcher;
	}

	public static SimpleEventDispatcher open() {
		return open(false);
	}

	long getDispatcherThreadId() {
		return dispatchThreadId;
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

	@Override
	public long getLoadAvg() {
		return loadAvg.getLoadAvg();
	}

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
				loadAvg.start();
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
						if (key != null) {
							handleEvent(eh, key, EventUtil.READ);
						}
					}
				}
				loadAvg.end();
			}
		} catch (ClosedSelectorException e) {
			serviceState.setClosed();
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
		Thread t = dispatchThread;
		if (t != null && serviceState.isOpen()) {
			// tell the dispatcher to stop processing events
			serviceState.setClosing();
			wakeup();
			ThreadUtil.interruptAndJoin(t);
			dispatchThread = null;

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
			Validation.notNull(handler);
			Validation.notNull(key);
			int interestedOps = key.interestOps();
			int newInterestedOps = handler.handle(ops);
			if (newInterestedOps != EventUtil.OP_DONT_CHANGE && newInterestedOps != interestedOps) {
				key.interestOps(newInterestedOps);
			}
		} catch (Exception e) {
			// TODO: log as trace?
			LOG.info("closing an event-handler due to an unexpected exception: " + ExceptionUtil.buildMessageChain(e), e);
			SafeCloseUtil.close(handler, LOG);
			if (e instanceof NullPointerException) {
				System.exit(0);
			}
		}
	}

	@Override
	public void register(EventHandler eventHandler, int ops) {
		newRegistrations.add(new NewRegistration(eventHandler, ops));
		wakeup();
	}

	private void wakeup() {
		Thread t = dispatchThread;
		if (t != null && t != Thread.currentThread()) {
			selector.wakeup();
		}
	}

	@Override
	public void changeOps(EventHandler eventHandler, int ops, boolean asap) {
		SelectableChannel channel = eventHandler.getChannel();
		SelectionKey key = channel.keyFor(selector);
		if (key == null) {
			// channel is not registered for this selector
			//TODO: notify a listener
			System.err.println("cannot change ops for a channel which is not yet registered, handler: " + eventHandler);
		} else {
			key.interestOps(ops);
			if (asap) {
				wakeup();
			}
		}
	}

	@Override
	public void changeHandler(EventHandler oldHandler, EventHandler newHandler, int ops, boolean asap) {
		// TODO Auto-generated method stub
		SelectableChannel channel = oldHandler.getChannel();
		Validation.isTrue(channel == newHandler.getChannel());
		SelectionKey key = channel.keyFor(selector);
		if (key == null) {
			System.err.println("cannot change handlers for a channel which is not yet registered, handler: " + oldHandler);
		} else {
			key.interestOps(ops);
			key.attach(newHandler);
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
			// TODO: this could be implemented more efficiently, is it required for high load?
			Iterator<NewRegistration> iter = newRegistrations.iterator();
			while (iter.hasNext()) {
				NewRegistration newReq = iter.next();
				if (newReq.eventHandler == eventHandler) {
					iter.remove();
					break;
				}
			}
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
		return (thread != null) && (thread.getId() == dispatchThreadId);
	}

	@Override
	public int getRegisteredOps(EventHandler eventHandler) {
		SelectionKey selectionKey = eventHandler.getChannel().keyFor(selector);
		if (selectionKey != null) {
			return selectionKey.interestOps();
		}
		return 0;
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
