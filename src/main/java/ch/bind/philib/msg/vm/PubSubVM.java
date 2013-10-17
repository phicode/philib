/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.msg.vm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.exp.msg.MessageHandler;
import ch.bind.philib.exp.msg.PubSub;
import ch.bind.philib.exp.msg.Subscription;
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.util.CowSet;
import ch.bind.philib.validation.Validation;

public final class PubSubVM implements PubSub {

	private static final Logger LOG = LoggerFactory.getLogger(PubSubVM.class);

	private final ConcurrentMap<String, Channel> chans = new ConcurrentHashMap<String, Channel>();

	private final ExecutorService executorService;

	private final Object subscriberLock = new Object();

	/**
	 * Creates a {@code DefaultTinyPubSub} which publishes messages through the
	 * provided {@code ExecutorService}.
	 */
	public PubSubVM(ExecutorService executorService) {
		Validation.notNull(executorService);
		this.executorService = executorService;
	}

	@Override
	public Subscription subscribe(String channelName, MessageHandler handler) {
		Validation.notNull(handler);
		synchronized (subscriberLock) {
			Channel chan = getOrCreateChan(channelName);
			Sub sub = chan.subscribe(handler);
			if (sub == null) {
				throw new IllegalArgumentException("double registration for channel='" + channelName + "' and handler: " + handler);
			}
			return sub;
		}
	}

	private void unsubscribe(Channel channel, Sub sub) {
		if (!channel.subs.remove(sub)) {
			return;
		}
		if (channel.subs.isEmpty()) {
			synchronized (subscriberLock) {
				if (channel.subs.isEmpty()) {
					chans.remove(channel.name);
				}
			}
		}
	}

	@Override
	public void publish(String channelName, Object message) {
		Validation.notNull(message);
		Channel chan = getChan(channelName);
		if (chan != null) {
			AsyncPublisher pub = new AsyncPublisher(chan, message);
			executorService.execute(pub);
		}
	}

	private Channel getChan(String channelName) {
		Validation.notNullOrEmpty(channelName);
		return chans.get(channelName);
	}

	private Channel getOrCreateChan(String channelName) {
		Channel chan = getChan(channelName);
		if (chan != null) {
			return chan;
		}
		chan = new Channel(channelName);
		Channel other = chans.putIfAbsent(channelName, chan);
		return other == null ? chan : other;
	}

	@Override
	public Map<String, Integer> activeChannels() {
		Map<String, Integer> rv = null;
		for (Entry<String, Channel> e : chans.entrySet()) {
			String name = e.getKey();
			Channel c = e.getValue();
			int num = c.subs.size();
			if (num > 0) {
				if (rv == null) {
					rv = new HashMap<String, Integer>();
				}
				rv.put(name, num);
			}
		}
		return rv == null ? Collections.<String, Integer> emptyMap() : rv;
	}

	private final class Channel {

		private final String name;

		private final CowSet<Sub> subs = new CowSet<Sub>(Sub.class);

		Channel(String name) {
			this.name = name;
		}

		Sub subscribe(MessageHandler handler) {
			Sub sub = new Sub(this, handler);
			if (subs.add(sub)) {
				return sub;
			}
			return null;
		}
	}

	private static final AtomicIntegerFieldUpdater<Sub> SUB_ACTIVE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(Sub.class, "active");

	private final class Sub implements Subscription {

		private final Channel channel;

		private final MessageHandler handler;

		volatile int active = 1;

		public Sub(Channel channel, MessageHandler handler) {
			this.channel = channel;
			this.handler = handler;
		}

		@Override
		public String getChannelName() {
			return channel.name;
		}

		@Override
		public void cancel() {
			if (SUB_ACTIVE_UPDATER.compareAndSet(this, 1, 0)) {
				PubSubVM.this.unsubscribe(channel, this);
			}
		}

		@Override
		public boolean isActive() {
			return active == 1;
		}

		@Override
		public String toString() {
			return "Subscription[active=" + isActive() + ", channel=" + getChannelName() + "]";
		}

		@Override
		public int hashCode() {
			// forward to the MessageHandler so that the CowSet reflects that
			// reference
			return System.identityHashCode(handler);
		}

		@Override
		public boolean equals(Object obj) {
			// a sub is just a wrapper for a MessageHandler
			// to identify equality the handler reference is compared
			if (obj == this) {
				return true;
			}
			if (obj instanceof Sub) {
				Sub o = (Sub) obj;
				return handler == o.handler;
			}
			return false;
		}
	}

	private static void publishMessage(Channel channel, Object message) {
		final Sub[] subs = channel.subs.getView();
		final String channelName = channel.name;
		for (Sub sub : subs) {
			if (sub.isActive()) {
				try {
					sub.handler.handleMessage(channelName, message);
				} catch (Exception e) {
					LOG.error("MessageHandler failed: " + ExceptionUtil.buildMessageChain(e));
				}
			}
		}
	}

	private final class AsyncPublisher implements Runnable {

		private final Channel channel;

		private final Object message;

		public AsyncPublisher(Channel channel, Object message) {
			this.channel = channel;
			this.message = message;
		}

		@Override
		public void run() {
			PubSubVM.publishMessage(channel, message);
		}
	}
}
