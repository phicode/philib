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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ServiceState;
import ch.bind.philib.validation.Validation;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */
public class ConcurrentEventDispatcher implements EventDispatcher {

	public enum ScaleStrategy {
		ROUND_ROBIN, //
		LEAST_CONNECTIONS, //
		LEAST_LOAD
	}

	// TODO: long -> Object map
	private final ConcurrentMap<Long, EventDispatcher> map = new ConcurrentHashMap<Long, EventDispatcher>();

	private final ServiceState serviceState = new ServiceState();

	private final EventDispatcher[] dispatchers;

	private final ScaleStrategy scaleStrategy;

	private final AtomicLong nextRoundRobinIdx = new AtomicLong(0);

	ConcurrentEventDispatcher(EventDispatcher[] dispatchers, ScaleStrategy scaleStrategy) {
		this.dispatchers = dispatchers;
		this.scaleStrategy = scaleStrategy;
		serviceState.setOpen();
	}

	public static EventDispatcher open() throws EventDispatcherCreationException {
		return open(ScaleStrategy.ROUND_ROBIN);
	}

	public static EventDispatcher open(int concurrency) throws EventDispatcherCreationException {
		return open(ScaleStrategy.ROUND_ROBIN, concurrency);
	}

	public static EventDispatcher open(ScaleStrategy scaleStrategy) throws EventDispatcherCreationException {
		return open(scaleStrategy, Runtime.getRuntime().availableProcessors());
	}

	public static EventDispatcher open(ScaleStrategy scaleStrategy, int concurrency) throws EventDispatcherCreationException {
		scaleStrategy = scaleStrategy != null ? scaleStrategy : ScaleStrategy.ROUND_ROBIN;
		concurrency = concurrency < 2 ? 2 : concurrency;
		EventDispatcher[] dispatchers = new EventDispatcher[concurrency];
		boolean collectLoadAverage = scaleStrategy == ScaleStrategy.LEAST_LOAD;
		for (int i = 0; i < concurrency; i++) {
			try {
				dispatchers[i] = SimpleEventDispatcher.open(collectLoadAverage);
			} catch (IOException e) {
				// close all event-dispatchers which have already been created
				for (int j = 0; j < i; j++) {
					SafeCloseUtil.close(dispatchers[j]);
				}
				throw new EventDispatcherCreationException("could not start " + concurrency + " dispatchers", e);
			}
		}
		return new ConcurrentEventDispatcher(dispatchers, scaleStrategy);
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
		EventDispatcher disp = createMappingOrThrow(eventHandler);
		disp.register(eventHandler, ops);
	}

	@Override
	public void unregister(EventHandler eventHandler) {
		Validation.notNull(eventHandler);
		long id = eventHandler.getEventHandlerId();
		EventDispatcher disp = map.remove(id);
		if (disp == null) {
			return;
		}
		disp.unregister(eventHandler);
	}

	@Override
	public void setTimeout(EventHandler eventHandler, long timeout, TimeUnit timeUnit) {
		EventDispatcher disp = findMappingOrThrow(eventHandler);
		disp.setTimeout(eventHandler, timeout, timeUnit);
	}

	@Override
	public void unsetTimeout(EventHandler eventHandler) {
		EventDispatcher disp = findMappingOrThrow(eventHandler);
		disp.unsetTimeout(eventHandler);
	}

	@Override
	public int getRegisteredOps(EventHandler eventHandler) {
		EventDispatcher disp = findMappingOrThrow(eventHandler);
		return disp.getRegisteredOps(eventHandler);
	}

	@Override
	public int getNumEventHandlers() {
		int c = 0;
		for (EventDispatcher ed : dispatchers) {
			c += ed.getNumEventHandlers();
		}
		return c;
	}

	@Override
	public long getLoadAvg() {
		long load = 0;
		for (EventDispatcher ed : dispatchers) {
			load += ed.getLoadAvg();
		}
		return load;
	}

	private EventDispatcher findMappingOrThrow(EventHandler eventHandler) {
		Validation.notNull(eventHandler);
		long id = eventHandler.getEventHandlerId();
		EventDispatcher disp = map.get(id);
		if (disp == null) {
			throw new IllegalStateException("event handler not registered, id=" + id + " : " + eventHandler);
		}
		return disp;
	}

	private EventDispatcher createMappingOrThrow(EventHandler eventHandler) {
		Validation.notNull(eventHandler);
		final long id = eventHandler.getEventHandlerId();
		EventDispatcher disp = findBestDispatcher();
		EventDispatcher existing = map.putIfAbsent(id, disp);
		if (existing != null) {
			throw new IllegalStateException("duplicate registration for event with id=" + id + " : " + eventHandler);
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

	private EventDispatcher findLeastConnections() {
		EventDispatcher best = dispatchers[0];
		int bestConnections = best.getNumEventHandlers();
		for (int i = 1; i < dispatchers.length; i++) {
			EventDispatcher disp = dispatchers[i];
			int numCon = disp.getNumEventHandlers();
			if (numCon < bestConnections) {
				best = disp;
				bestConnections = numCon;
			}
		}
		return best;
	}

	private EventDispatcher findLeastLoad() {
		EventDispatcher best = dispatchers[0];
		long bestLoadAvg = best.getLoadAvg();
		for (int i = 1; i < dispatchers.length; i++) {
			EventDispatcher disp = dispatchers[i];
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
}
