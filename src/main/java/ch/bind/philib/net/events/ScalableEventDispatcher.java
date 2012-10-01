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
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ServiceState;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class ScalableEventDispatcher implements EventDispatcher {

	public enum ScaleStrategy {
		ROUND_ROBIN, //
		LEAST_CONNECTIONS, //
		LEAST_LOAD
	}

	// TODO: long -> Object map
	private final ConcurrentMap<Long, EventDispatcher> map = new ConcurrentHashMap<Long, EventDispatcher>();

	private final ServiceState serviceState = new ServiceState();

	private final RichEventDispatcher[] dispatchers;

	private final long[] threadIds;

	private final ScaleStrategy scaleStrategy;

	private final AtomicLong nextRoundRobinIdx = new AtomicLong(0);

	private ScalableEventDispatcher(RichEventDispatcher[] dispatchers, long[] threadIds, ScaleStrategy scaleStrategy) {
		this.dispatchers = dispatchers;
		this.threadIds = threadIds;
		this.scaleStrategy = scaleStrategy;
		Arrays.sort(threadIds);
		serviceState.setOpen();
	}

	public static EventDispatcher open() {
		return open(ScaleStrategy.ROUND_ROBIN);
	}

	public static EventDispatcher open(int concurrency) {
		return open(ScaleStrategy.ROUND_ROBIN, concurrency);
	}

	public static EventDispatcher open(ScaleStrategy scaleStrategy) {
		return open(scaleStrategy, Runtime.getRuntime().availableProcessors());
	}

	public static EventDispatcher open(ScaleStrategy scaleStrategy, int concurrency) {
		scaleStrategy = scaleStrategy != null ? scaleStrategy : ScaleStrategy.ROUND_ROBIN;
		concurrency = concurrency >= 2 ? concurrency : 2;
		RichEventDispatcher[] dispatchers = new RichEventDispatcher[concurrency];
		long[] threadIds = new long[concurrency];
		boolean collectLoadAverage = scaleStrategy == ScaleStrategy.LEAST_LOAD;
		for (int i = 0; i < concurrency; i++) {
			try {
				SimpleEventDispatcher sed = SimpleEventDispatcher.open(collectLoadAverage);
				dispatchers[i] = sed;
				threadIds[i] = sed.getDispatcherThreadId();
			} catch (Exception e) {
				// close all event-dispatchers which have already been created
				for (int j = 0; j < i; j++) {
					SafeCloseUtil.close(dispatchers[j]);
				}
				throw new EventDispatcherCreationException("could not start " + concurrency + " dispatchers", e);
			}
		}
		return new ScalableEventDispatcher(dispatchers, threadIds, scaleStrategy);
	}

	@Override
	public void close() throws IOException {
		serviceState.setClosing();
		for (EventDispatcher ed : dispatchers) {
			SafeCloseUtil.close(ed);
		}
		map.clear();
		serviceState.setClosed();
	}

	@Override
	public boolean isOpen() {
		return serviceState.isOpen();
	}

	@Override
	public void register(EventHandler eventHandler, int ops) {
		EventDispatcher disp = findMapping(eventHandler);
		if (disp == null) {
			disp = createMapping(eventHandler);
		}
		disp.register(eventHandler, ops);
	}

	private EventDispatcher findMapping(EventHandler eventHandler) {
		if (eventHandler == null) {
			return null;
		}
		long id = eventHandler.getEventHandlerId();
		return map.get(id);
	}

	private EventDispatcher createMapping(EventHandler eventHandler) {
		long id = eventHandler.getEventHandlerId();
		EventDispatcher disp = map.get(id);
		if (disp == null) {
			disp = findBestDispatcher();
			EventDispatcher registeredDisp = map.putIfAbsent(id, disp);
			if (registeredDisp != null) {
				disp = registeredDisp;
			}
		}
		return disp;
	}

	private EventDispatcher findBestDispatcher() {
		switch (scaleStrategy) {
		case LEAST_CONNECTIONS:
			return findLeastConnections();
		case LEAST_LOAD:
			return findLeastLoad();
		case ROUND_ROBIN:
		default:
			return findRoundRobin();
		}
	}

	// TODO: find something more effective then looping over all dispatchers
	private RichEventDispatcher findLeastConnections() {
		RichEventDispatcher best = dispatchers[0];
		int bestConnections = best.getNumEventHandlers();
		for (int i = 1; i < dispatchers.length; i++) {
			RichEventDispatcher disp = dispatchers[i];
			int numCon = disp.getNumEventHandlers();
			if (numCon < bestConnections) {
				best = disp;
				bestConnections = numCon;
			}
		}
		return best;
	}

	// TODO: find something more effective then looping over all dispatchers
	private EventDispatcher findLeastLoad() {
		RichEventDispatcher best = dispatchers[0];
		long bestLoadAvg = best.getLoadAvg();
		for (int i = 1; i < dispatchers.length; i++) {
			RichEventDispatcher disp = dispatchers[i];
			long loadAvg = disp.getLoadAvg();
			if (loadAvg < bestLoadAvg) {
				best = disp;
				bestLoadAvg = loadAvg;
			}
		}
		return best;
	}

	private EventDispatcher findRoundRobin() {
		long dispIdx = nextRoundRobinIdx.getAndIncrement();
		int realDispIdx = (int) (dispIdx % dispatchers.length);
		return dispatchers[realDispIdx];
	}

	@Override
	public void changeOps(EventHandler eventHandler, int ops, boolean asap) {
		EventDispatcher disp = findMapping(eventHandler);
		if (disp != null) {
			disp.changeOps(eventHandler, ops, asap);
		} else {
			// TODO: notify listener
			System.out.println("event handler is not registered: " + eventHandler);
		}
	}

	@Override
	public void changeHandler(EventHandler oldHandler, EventHandler newHandler, int ops, boolean asap) {
		if (findMapping(newHandler) != null) {
			System.out.println("an event-dispatcher is already registered for " + newHandler);
			return;
		}
		long oldId = oldHandler.getEventHandlerId();
		long newId = newHandler.getEventHandlerId();
		EventDispatcher disp = map.get(oldId);
		if (disp != null) {
			map.put(newId, disp);
			disp.changeHandler(oldHandler, newHandler, ops, asap);
			map.remove(oldId);
		} else {
			//TODO: error and stuff
			System.out.println("handler has no dispatcher: " + oldHandler);
		}
	}

	@Override
	public void unregister(EventHandler eventHandler) {
		if (eventHandler != null) {
			EventDispatcher disp = map.remove(eventHandler.getEventHandlerId());
			if (disp != null) {
				disp.unregister(eventHandler);
			} else {
				// TODO: notify listener
				System.out.println("event handler is not registered: " + eventHandler);
			}
		}
	}

	@Override
	public boolean isEventDispatcherThread(Thread thread) {
		return (thread != null) && (Arrays.binarySearch(threadIds, thread.getId()) >= 0);
	}

	@Override
	public void registerForRedeliverPartialReads(EventHandler eventHandler) {
		EventDispatcher disp = findMapping(eventHandler);
		if (disp != null) {
			disp.registerForRedeliverPartialReads(eventHandler);
		} else {
			// TODO: notify listener
			System.out.println("event handler is not registered: " + eventHandler);
		}
	}

	@Override
	public void unregisterFromRedeliverPartialReads(EventHandler eventHandler) {
		EventDispatcher disp = findMapping(eventHandler);
		if (disp != null) {
			disp.unregisterFromRedeliverPartialReads(eventHandler);
		}
		// TODO: notify listener
		System.out.println("event handler is not registered: " + eventHandler);
	}

	@Override
	public int getRegisteredOps(EventHandler eventHandler) {
		EventDispatcher disp = findMapping(eventHandler);
		if (disp != null) {
			return disp.getRegisteredOps(eventHandler);
		}
		// TODO: notify listener
		System.out.println("event handler is not registered: " + eventHandler);
		return 0;
	}
}
