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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ServiceState;
import ch.bind.philib.validation.Validation;

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

	private final Map<EventHandler, EventDispatcher> map = new ConcurrentHashMap<EventHandler, EventDispatcher>();

	private final ServiceState serviceState = new ServiceState();

	private final EventDispatcher[] dispatchers;

	private final long[] threadIds;

	private final ScaleStrategy scaleStrategy;

	private ScalableEventDispatcher(EventDispatcher[] dispatchers, long[] threadIds, ScaleStrategy scaleStrategy) {
		this.dispatchers = dispatchers;
		this.threadIds = threadIds;
		this.scaleStrategy = scaleStrategy;
		Arrays.sort(threadIds);
		serviceState.setOpen();
	}

	public static ScalableEventDispatcher open() {
		return open(ScaleStrategy.ROUND_ROBIN);
	}

	public static ScalableEventDispatcher open(ScaleStrategy scaleStrategy) {
		return open(scaleStrategy, Runtime.getRuntime().availableProcessors());
	}

	public static ScalableEventDispatcher open(ScaleStrategy scaleStrategy, int concurrency) {
		scaleStrategy = scaleStrategy != null ? scaleStrategy : ScaleStrategy.ROUND_ROBIN;
		concurrency = concurrency >= 1 ? concurrency : 1;
		EventDispatcher[] dispatchers = new EventDispatcher[concurrency];
		long[] threadIds = new long[concurrency];
		for (int i = 0; i < concurrency; i++) {
			try {
				SimpleEventDispatcher sed = SimpleEventDispatcher.open();
				dispatchers[i] = sed;
				threadIds[i] = sed.getDispatcherThreadId();
			} catch (Exception e) {
				// close all event-dispatchers which have already been created
				for (int j = 0; j < i; j++) {
					SafeCloseUtil.close(dispatchers[i]);
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

	@Override
	public void reRegister(EventHandler eventHandler, int ops, boolean asap) {
		EventDispatcher disp = findMapping(eventHandler);
		if (disp != null) {
			disp.reRegister(eventHandler, ops, asap);
		}
	}

	@Override
	public void unregister(EventHandler eventHandler) {
		// TODO Auto-generated method stub

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
		}
	}

	@Override
	public void unregisterFromRedeliverPartialReads(EventHandler eventHandler) {
		EventDispatcher disp = findMapping(eventHandler);
		if (disp != null) {
			disp.unregisterFromRedeliverPartialReads(eventHandler);
		}
	}
}
