package ch.bind.philib.msg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.validation.Validation;

public class SimpleMessageBroker implements MessageBroker {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleMessageBroker.class);

	private final Map<String, Channel> channels = new HashMap<String, Channel>();

	private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

	private final Lock rlock = rwlock.readLock();

	private final Lock wlock = rwlock.writeLock();

	private final Channel deadLetterChannel = new Channel("dead-letter-channel");

	private final ExecutorService executorService;

	/**
	 * Creates a {@code SimpleMessageBroker} which publishes messages through
	 * the provided {@code ExecutorService}.
	 */
	public SimpleMessageBroker(ExecutorService executorService) {
		Validation.notNull(executorService);
		this.executorService = executorService;
	}

	@Override
	public Subscription subscribe(String channel, MessageHandler handler) {
		Validation.notNull(handler);
		wlock.lock();
		try {
			Channel chan = getChannel(channel);
			if (chan == null) {
				chan = new Channel(channel);
				channels.put(channel, chan);
			}
			return chan.subscribe(handler);
		} finally {
			wlock.unlock();
		}
	}

	private void unsubscribe(Channel channel, Sub sub) {
		wlock.lock();
		try {
			boolean empty = false;
			synchronized (channel.subs) {
				channel.subs.remove(sub);
				empty = channel.subs.isEmpty();
			}
			if (empty && channel != deadLetterChannel) {
				channels.remove(channel.name);
			}
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public void addDeadLetterHandler(DeadLetterHandler handler) {
		Validation.notNull(handler);
		wlock.lock();
		try {
			return deadLetterChannel.subscribe(handler);
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public void publishSync(String channel, Object message) throws InterruptedException {
		Validation.notNull(message);
		Channel chan = getPublishChannel(channel);
		chan.publishSync(message);
	}

	@Override
	public void publishAsync(String channel, Object message) {
		Validation.notNull(message);
		Channel chan = getPublishChannel(channel);
		chan.publishAsync(message);
	}

	private Channel getPublishChannel(String channel) {
		rlock.lock();
		try {
			Channel chan = getChannel(channel);
			return chan != null ? chan : deadLetterChannel;
		} finally {
			rlock.unlock();
		}
	}

	private Channel getChannel(String channel) {
		// fast path without channel null-or-empty check
		Channel chan = channels.get(channel);
		if (chan == null) {
			// channel does not exist, verify that the supplied name is valid
			Validation.notNullOrEmpty(channel);
		}
		return chan;
	}

	private final class Channel {

		private final String name;

		private final Set<Sub> subs = new HashSet<Sub>();

		Channel(String name) {
			this.name = name;
		}

		Sub subscribe(MessageHandler handler) {
			Sub sub = new Sub(this, handler);
			synchronized (subs) {
				subs.add(sub);
			}
			return sub;
		}

		void publishSync(Object message) throws InterruptedException {
			Semaphore sem = new Semaphore(0);
			int n = 0;
			synchronized (subs) {
				for (Sub sub : subs) {
					Runnable r = new PublishRunner(sub, message, sem);
					executorService.execute(r);
					n++;
				}
			}
			sem.acquire(n);
		}

		void publishAsync(Object message) {
			synchronized (subs) {
				for (Sub sub : subs) {
					Runnable r = new PublishRunner(sub, message, null);
					executorService.execute(r);
				}
			}
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
				SimpleMessageBroker.this.unsubscribe(channel, this);
			}
		}

		@Override
		public boolean isActive() {
			return handler.get() != null;
		}
	}

	private static final class PublishRunner implements Runnable {

		private final Sub sub;

		private final Object message;

		private final Semaphore sem;

		public PublishRunner(Sub sub, Object message, Semaphore sem) {
			this.sub = sub;
			this.message = message;
			this.sem = sem;
		}

		@Override
		public void run() {
			try {
				MessageHandler handler = sub.handler.get();
				if (handler != null) {
					handler.handleMessage(message);
				}
			} catch (Exception e) {
				LOG.error("a MessageHandler unexpectedly failed: " + ExceptionUtil.buildMessageChain(e));
			} finally {
				if (sem != null) {
					sem.release();
				}
			}
		}
	}
}
