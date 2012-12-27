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
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.lang.ServiceState;
import ch.bind.philib.lang.ThreadUtil;
import ch.bind.philib.util.SimpleTimeoutMap;
import ch.bind.philib.util.LoadAvg;
import ch.bind.philib.util.LoadAvgNoop;
import ch.bind.philib.util.LoadAvgSimple;
import ch.bind.philib.util.TimeoutMap;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
// TODO: verify thread safety
public final class SimpleEventDispatcher implements EventDispatcher, Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleEventDispatcher.class);

	private static final AtomicLong NAME_SEQ = new AtomicLong(0);

	private static final int LOAD_AVG_SECONDS = 60;

	private final Queue<NewRegistration> newRegistrations = new ConcurrentLinkedQueue<NewRegistration>();

	private final ServiceState serviceState = new ServiceState();

	private final Selector selector;

	private final LoadAvg loadAvg;

	private final Thread dispatchThread;

	private volatile int numHandlers;

	private final TimeoutMap<Long, EventHandler> upcomingTimeouts = new SimpleTimeoutMap<Long, EventHandler>();

	private SimpleEventDispatcher(Selector selector, LoadAvg loadAvg) {
		this.selector = selector;
		this.loadAvg = loadAvg;
		String threadName = getClass().getSimpleName() + '-' + NAME_SEQ.getAndIncrement();
		this.dispatchThread = ThreadUtil.createAndStartForeverRunner(this, threadName);
	}

	public static SimpleEventDispatcher open(boolean collectLoadAverage) throws SelectorCreationException {
		Selector selector;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new SelectorCreationException(e);
		}
		LoadAvg loadAvg = collectLoadAverage ? LoadAvgSimple.forSeconds(LOAD_AVG_SECONDS) : LoadAvgNoop.INSTANCE;
		return new SimpleEventDispatcher(selector, loadAvg);
	}

	public static SimpleEventDispatcher open() throws SelectorCreationException {
		return open(false);
	}

	@Override
	public boolean isOpen() {
		return serviceState.isOpen();
	}

	@Override
	public int getNumEventHandlers() {
		return numHandlers;
	}

	@Override
	public long getLoadAvg() {
		return loadAvg.getLoadAvg();
	}

	private int select() throws IOException, ClosedSelectorException {
		if (serviceState.isOpen()) {
			updateRegistrations();
			long msUntilNextTimeout = upcomingTimeouts.getTimeToNextTimeout();
			if (msUntilNextTimeout <= 0) {
				return selector.selectNow();
			}
			else {
				long selectTimeout = Math.min(msUntilNextTimeout, 10000L);
				return selector.select(selectTimeout);
			}
		}
		return 0;
	}

	@Override
	public void close() {
		if (serviceState.isOpen()) {
			// tell the dispatcher to stop processing events
			serviceState.setClosing();

			Thread currentThread = Thread.currentThread();
			if (currentThread != dispatchThread) {
				wakeup();

				// wait a moment for pending events to be processed
				try {
					dispatchThread.join(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
				if (dispatchThread.isAlive()) {
					ThreadUtil.interruptAndJoin(dispatchThread, 500);
				}
			}

			for (SelectionKey key : selector.keys()) {
				if (key.isValid()) {
					Object att = key.attachment();
					if (att instanceof EventHandler) {
						EventHandler e = (EventHandler) att;
						key.cancel();
						SafeCloseUtil.close(e, LOG);
					}
				}
			}

			SafeCloseUtil.close(selector, LOG);

			for (NewRegistration newReg : newRegistrations) {
				SafeCloseUtil.close(newReg.getEventHandler(), LOG);
			}

			upcomingTimeouts.clear();

			serviceState.setClosed();
		}
	}

	private void handleReadyKey(final SelectionKey key) {
		if (key == null) {
			return;
		}
		final EventHandler eventHandler = (EventHandler) key.attachment();
		if (eventHandler == null) {
			// cancelled key
			return;
		}
		if (key.isValid()) {
			handleEvent(eventHandler, key, key.readyOps());
		}
		else {
			SafeCloseUtil.close(eventHandler, LOG);
		}
	}

	private void handleEvent(final EventHandler handler, final SelectionKey key, final int ops) {
		try {
			int oldInterestedOps = key.interestOps();
			int newInterestedOps = handler.handleOps(ops);
			if (newInterestedOps != oldInterestedOps && key.isValid()) {
				key.interestOps(newInterestedOps);
			}
		} catch (Exception e) {
			// TODO: notify session-manager
			LOG.info("closing an event-handler due to an unexpected exception: " + ExceptionUtil.buildMessageChain(e) + ", thread: "
					+ Thread.currentThread());
			SafeCloseUtil.close(handler, LOG);
		}
	}

	@Override
	public void register(EventHandler eventHandler, int ops) {
		SelectableChannel channel = eventHandler.getChannel();
		SelectionKey key = channel.keyFor(selector);
		if (key != null) {
			key.interestOps(ops);
		}
		else {
			newRegistrations.add(new NewRegistration(eventHandler, ops));
		}
		wakeup();
	}

	@Override
	public void setTimeout(EventHandler eventHandler, long timeout) {
		long handlerId = eventHandler.getEventHandlerId();
		upcomingTimeouts.add(timeout, handlerId, eventHandler);
		wakeup();
	}

	@Override
	public void unsetTimeout(EventHandler eventHandler) {
		long handlerId = eventHandler.getEventHandlerId();
		upcomingTimeouts.remove(handlerId);
		// no wakeup needed
	}

	private void updateRegistrations() {
		NewRegistration reg = null;
		while ((reg = newRegistrations.poll()) != null) {
			EventHandler eventHandler = reg.getEventHandler();
			SelectableChannel channel = eventHandler.getChannel();
			SelectionKey key = channel.keyFor(selector);
			if (key != null) {
				key.interestOps(reg.getOps());
			}
			else {
				try {
					int ops = reg.getOps();
					channel.register(selector, ops, eventHandler);
				} catch (ClosedChannelException e) {
					SafeCloseUtil.close(eventHandler, LOG);
				}
			}
		}
		numHandlers = selector.keys().size();
	}

	private void wakeup() {
		if (dispatchThread != Thread.currentThread()) {
			selector.wakeup();
		}
	}

	@Override
	public void unregister(EventHandler eventHandler) {
		SelectableChannel channel = eventHandler.getChannel();
		SelectionKey key = channel.keyFor(selector);
		if (key != null) {
			key.cancel();
			key.attach(null);
			upcomingTimeouts.remove(eventHandler.getEventHandlerId());
			wakeup();
		}
		else {
			// handle event-handlers which unregister before they were added to
			// the selector

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

	@Override
	public void run() {
		if (serviceState.isUninitialized()) {
			serviceState.setOpen();
		}
		try {
			while (serviceState.isOpen()) {
				int num = select();
				long tStartNs = System.nanoTime();
				if (num > 0) {
					Set<SelectionKey> selected = selector.selectedKeys();
					for (SelectionKey key : selected) {
						handleReadyKey(key);
					}
					selected.clear();
				}
				handleTimeouts();
				long tWorkNs = System.nanoTime() - tStartNs;
				loadAvg.logWorkNs(tWorkNs);
			}
		} catch (IOException e) {
			LOG.error("select() failed", e);
			close();
		} catch (ClosedSelectorException e) {
			close();
		}
	}

	private void handleTimeouts() {
		Map.Entry<Long, EventHandler> timeout = null;
		while ((timeout = upcomingTimeouts.pollTimeout()) != null) {
			handleTimeout(timeout.getValue());
		}
	}

	private void handleTimeout(EventHandler handler) {
		try {
			boolean keepRunning = handler.handleTimeout();
			if (!keepRunning) {
				SafeCloseUtil.close(handler, LOG);
			}
		} catch (Exception e) {
			// TODO: notify session-manager
			LOG.info(
					"closing an event-handler due to an unexpected exception: " + ExceptionUtil.buildMessageChain(e) + ", thread: "
							+ Thread.currentThread(), e);
			SafeCloseUtil.close(handler, LOG);
		}
	}
}
