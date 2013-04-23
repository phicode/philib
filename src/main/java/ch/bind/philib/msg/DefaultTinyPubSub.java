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

package ch.bind.philib.msg;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.validation.Validation;

public final class DefaultTinyPubSub implements TinyPubSub {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultTinyPubSub.class);

	private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

	private final Lock rlock = rwlock.readLock();

	private final Lock wlock = rwlock.writeLock();

	private final Map<String, Channel> channels = new HashMap<String, Channel>();

	private final SimpleCowList<MessageHandler> deadLetterHandlers = new SimpleCowList<MessageHandler>(MessageHandler.class);

	private final ExecutorService executorService;

	/**
	 * Creates a {@code DefaultTinyPubSub} which publishes messages through the
	 * provided {@code ExecutorService}.
	 */
	public DefaultTinyPubSub(ExecutorService executorService) {
		Validation.notNull(executorService);
		this.executorService = executorService;
	}

	@Override
	public Subscription subscribe(String channelName, MessageHandler handler) {
		Validation.notNull(handler);
		wlock.lock();
		try {
			Channel chan = getChannel(channelName);
			if (chan == null) {
				chan = new Channel(channelName);
				channels.put(channelName, chan);
			}
			return chan.subscribe(handler);
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public Subscription forward(String fromChannelName, String toChannelName) {
		Validation.notNullOrEmpty(fromChannelName);
		Validation.notNullOrEmpty(toChannelName);
		MessageHandler handler = new Forwarder(toChannelName, this);
		return subscribe(fromChannelName, handler);
	}

	private void unsubscribe(Channel channel, Sub sub) {
		if (!channel.subs.remove(sub)) {
			return;
		}
		if (channel.subs.isEmpty()) {
			wlock.lock();
			try {
				if (channel.subs.isEmpty()) {
					channels.remove(channel.name);
				}
			} finally {
				wlock.unlock();
			}
		}
	}

	@Override
	public void addDeadLetterHandler(MessageHandler handler) {
		Validation.notNull(handler);
		deadLetterHandlers.add(handler);
	}

	@Override
	public void removeDeadLetterHandler(MessageHandler handler) {
		Validation.notNull(handler);
		deadLetterHandlers.remove(handler);
	}

	private void syncNotifyDeadLetterHandlers(String channelName, Object message) {
		final MessageHandler[] dlhs = deadLetterHandlers.getView();
		for (MessageHandler dlh : dlhs) {
			try {
				dlh.handleMessage(channelName, message);
			} catch (Exception e) {
				LOG.error("dead-letter MessageHandler failed: " + ExceptionUtil.buildMessageChain(e));
			}
		}
	}

	private void asyncNotifyDeadLetterHandlers(final String channelName, final Object message) {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				syncNotifyDeadLetterHandlers(channelName, message);
			}
		});
	}

	@Override
	public void publishSync(String channelName, Object message) {
		Validation.notNull(message);
		Channel chan = rlockedGetChannel(channelName);
		if (chan == null) {
			syncNotifyDeadLetterHandlers(channelName, message);
		}
		else {
			chan.publishSync(message);
		}
	}

	@Override
	public void publishAsync(String channelName, Object message) {
		Validation.notNull(message);
		Channel chan = rlockedGetChannel(channelName);
		if (chan == null) {
			asyncNotifyDeadLetterHandlers(channelName, message);
		}
		else {
			chan.publishAsync(message);
		}
	}

	private Channel rlockedGetChannel(String channelName) {
		rlock.lock();
		try {
			return getChannel(channelName);
		} finally {
			rlock.unlock();
		}
	}

	private Channel getChannel(String channelName) {
		// fast path without channel null-or-empty check
		Channel chan = channels.get(channelName);
		if (chan == null) {
			// channel does not exist, verify that the supplied name is valid
			Validation.notNullOrEmpty(channelName);
		}
		return chan;
	}

	private final class Channel {

		private final String name;

		private final SimpleCowList<Sub> subs = new SimpleCowList<Sub>(Sub.class);

		Channel(String name) {
			this.name = name;
		}

		Sub subscribe(MessageHandler handler) {
			Sub sub = new Sub(this, handler);
			subs.add(sub);
			return sub;
		}

		void publishSync(Object message) {
			publishMessage(this, message);
		}

		void publishAsync(Object message) {
			AsyncMessagePublisher msgPub = new AsyncMessagePublisher(this, message);
			executorService.execute(msgPub);
		}
	}

	private final class Sub implements Subscription {

		private final Channel channel;

		private final AtomicReference<MessageHandler> handler;

		public Sub(Channel channel, MessageHandler handler) {
			this.channel = channel;
			this.handler = new AtomicReference<MessageHandler>(handler);
		}

		@Override
		public String getChannelName() {
			return channel.name;
		}

		@Override
		public void cancel() {
			if (handler.getAndSet(null) != null) {
				DefaultTinyPubSub.this.unsubscribe(channel, this);
			}
		}

		@Override
		public boolean isActive() {
			return handler.get() != null;
		}
	}

	private void publishMessage(Channel channel, Object message) {
		boolean handled = false;
		final Sub[] subs = channel.subs.getView();
		final String channelName = channel.name;
		for (Sub sub : subs) {
			MessageHandler handler = sub.handler.get();
			if (handler != null) {
				try {
					handled |= handler.handleMessage(channelName, message);
				} catch (Exception e) {
					LOG.error("MessageHandler failed: " + ExceptionUtil.buildMessageChain(e));
				}
			}
		}
		if (!handled) {
			// sync-dead-letter handling is ok in this case since we are another
			// thread in the async case or calling in
			// synchronous mode
			syncNotifyDeadLetterHandlers(channelName, message);
		}
	}

	private final class AsyncMessagePublisher implements Runnable {

		private final Channel channel;

		private final Object message;

		public AsyncMessagePublisher(Channel channel, Object message) {
			this.channel = channel;
			this.message = message;
		}

		@Override
		public void run() {
			DefaultTinyPubSub.this.publishMessage(channel, message);
		}
	}

	private static final class Forwarder implements MessageHandler {

		private final String to;

		private final TinyPubSub pubsub;

		Forwarder(String to, TinyPubSub pubsub) {
			this.to = to;
			this.pubsub = pubsub;
		}

		@Override
		public boolean handleMessage(String channelName, Object message) {
			pubsub.publishSync(to, message);
			return true;
		}
	}
}
