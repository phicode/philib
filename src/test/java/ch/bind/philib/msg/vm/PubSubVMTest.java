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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.bind.philib.msg.MessageHandler;
import ch.bind.philib.msg.PubSub;
import ch.bind.philib.msg.Subscription;

public class PubSubVMTest {

	private ExecutorService singleThreadExecutor;

	@BeforeMethod
	public void beforeMethod() {
		singleThreadExecutor = Executors.newSingleThreadExecutor();
	}

	@AfterMethod
	public void afterMethod() {
		List<Runnable> runnables = singleThreadExecutor.shutdownNow();
		assertTrue(runnables.isEmpty(), runnables.toString());
	}

	@Test(timeOut = 500)
	public void emptyPublish() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		pubsub.publish("foo", "bar");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void noNullSubscribeChannelName() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe(null, mh);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void noEmptySubscribeChannelName() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("", mh);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void noNullSubscribeMessageHandler() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		pubsub.subscribe("", null);
	}

	@Test(timeOut = 500)
	public void standardPublish() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh);
		pubsub.publish("foo", "bar");
		pubsub.publish("foo", "baz");
		mh.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void publishToTwoSubscribers() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh1 = new RecordingMessageHandler();
		RecordingMessageHandler mh2 = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh1);
		pubsub.subscribe("foo", mh2);
		pubsub.publish("foo", "bar");
		pubsub.publish("foo", "baz");
		mh1.assertMessages("foo", "bar", "baz");
		mh2.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void unsubscribe() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh1 = new RecordingMessageHandler();
		RecordingMessageHandler mh2 = new RecordingMessageHandler();

		Subscription s1 = pubsub.subscribe("foo", mh1);
		Subscription s2 = pubsub.subscribe("foo", mh2);
		assertNotNull(s1);
		assertNotNull(s2);
		assertTrue(s1.isActive() && s2.isActive());

		pubsub.publish("foo", "bar");
		mh1.assertMessages("foo", "bar");
		s1.cancel();
		assertTrue(!s1.isActive() && s2.isActive());

		pubsub.publish("foo", "baz");
		mh2.assertMessages("foo", "bar", "baz");
		s2.cancel();
		assertTrue(!s1.isActive() && !s2.isActive());

		pubsub.publish("foo", "xxx");
		s2.cancel(); // noop

		mh1.assertMessages("foo", "bar");
		mh2.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void noDeliveryOnOtherChannels() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh);
		pubsub.publish("foo", "bar");
		pubsub.publish("yyy", "zzz");
		mh.assertMessages("foo", "bar");
	}

	@Test(timeOut = 500)
	public void unsubscribeWhilePublishingIsRunning() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh1 = new RecordingMessageHandler();
		RecordingMessageHandler mh2 = new RecordingMessageHandler();

		Subscription s1 = pubsub.subscribe("foo", mh1);
		Subscription s2 = pubsub.subscribe("foo", mh2);

		// lock the message-handlers so that the notification publisher can not
		// proceed
		mh1.lock.lock();
		mh2.lock.lock();
		pubsub.publish("foo", "bar");

		// wait for the publisher to start working
		// hasQueueLength is unreliable this is why getQueueLength is used here
		while (mh1.lock.getQueueLength() == 0 && mh2.lock.getQueueLength() == 0) {
			Thread.yield();
		}
		RecordingMessageHandler active, cancel;
		// now cancel the other subscription and make the publisher unstuck
		if (mh1.lock.hasQueuedThreads()) {
			assertFalse(mh2.lock.hasQueuedThreads());
			active = mh1;
			cancel = mh2;
			s2.cancel();
		} else {
			assertFalse(mh1.lock.hasQueuedThreads());
			active = mh2;
			cancel = mh1;
			s1.cancel();
		}

		active.assertMessages("foo"); // nothing received yet
		mh1.lock.unlock();
		mh2.lock.unlock();
		active.assertMessages("foo", "bar");
		cancel.assertMessages("foo");
	}

	@Test(timeOut = 500)
	public void dontFailOnSubscriberException() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("foo", new ThrowingMessageHandler());
		pubsub.subscribe("foo", mh);
		pubsub.publish("foo", "bar");
		mh.assertMessages("foo", "bar");
	}

	@Test(timeOut = 5000)
	public void simpleFanInFanOut() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		new FanOut("root", "1.1", "1.2", pubsub);
		new FanOut("1.1", "2.1", "2.2", pubsub);
		new FanOut("1.2", "2.2", "2.3", pubsub);
		new FanIn("2.1", "2.2", "3.1", pubsub);
		new FanIn("2.2", "2.3", "3.2", pubsub);
		new FanIn("3.1", "3.2", "end", pubsub);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("end", mh);
		pubsub.publish("root", "bar");
		mh.assertMessages("end", "bar", "bar", "bar", "bar", "bar", "bar");
	}

	@Test(timeOut = 5000)
	public void simpleChain() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		new Forwarder("0", "1", pubsub);
		RecordingMessageHandler tail = new RecordingMessageHandler();
		pubsub.subscribe("1000", tail);
		for (int i = 1; i < 1000; i++) {
			new Forwarder(Integer.toString(i), Integer.toString(i + 1), pubsub);
		}
		pubsub.publish("0", "bar");
		tail.assertMessages("1000", "bar");
		pubsub.publish("0", "baz");
		tail.assertMessages("1000", "bar", "baz");
	}

	@Test(timeOut = 5000)
	public void ring() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RingNode[] nodes = new RingNode[1000];
		for (int i = 0; i < 1000; i++) {
			String from = Integer.toString(i);
			String to = i == 999 ? "0" : Integer.toString(i + 1);
			nodes[i] = new RingNode(from, to, pubsub);
		}
		pubsub.publish("0", 1000000L);
		// 1000 messages for each node
		for (RingNode node : nodes) {
			while (node.msgCount.get() != 1000) {
				Thread.yield();
			}
		}
	}

	@Test(timeOut = 500, expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "double registration for channel='foo' and handler: RecordingMessageHandler")
	public void reregister() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		MessageHandler mh = new RecordingMessageHandler();
		Subscription s = pubsub.subscribe("foo", mh);
		assertNotNull(s);
		pubsub.subscribe("foo", mh);
	}

	@Test(timeOut = 500)
	public void activeChannels() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		assertTrue(pubsub.activeChannels().isEmpty());
		MessageHandler mh1 = new RecordingMessageHandler();
		MessageHandler mh2 = new RecordingMessageHandler();

		Subscription s1 = pubsub.subscribe("foo", mh1);
		Map<String, Integer> ac = pubsub.activeChannels();
		assertEquals(ac.size(), 1);
		assertEquals(ac.get("foo"), Integer.valueOf(1));

		Subscription s2 = pubsub.subscribe("foo", mh2);
		ac = pubsub.activeChannels();
		assertEquals(ac.size(), 1);
		assertEquals(ac.get("foo"), Integer.valueOf(2));

		Subscription s3 = pubsub.subscribe("bar", mh1);
		ac = pubsub.activeChannels();
		assertEquals(ac.size(), 2);
		assertEquals(ac.get("foo"), Integer.valueOf(2));
		assertEquals(ac.get("bar"), Integer.valueOf(1));

		Subscription s4 = pubsub.subscribe("baz", mh2);
		ac = pubsub.activeChannels();
		assertEquals(ac.size(), 3);
		assertEquals(ac.get("foo"), Integer.valueOf(2));
		assertEquals(ac.get("bar"), Integer.valueOf(1));
		assertEquals(ac.get("baz"), Integer.valueOf(1));

		s1.cancel();
		ac = pubsub.activeChannels();
		assertEquals(ac.size(), 3);
		assertEquals(ac.get("foo"), Integer.valueOf(1));
		assertEquals(ac.get("bar"), Integer.valueOf(1));
		assertEquals(ac.get("baz"), Integer.valueOf(1));

		s2.cancel();
		ac = pubsub.activeChannels();
		assertEquals(ac.size(), 2);
		assertEquals(ac.get("bar"), Integer.valueOf(1));
		assertEquals(ac.get("baz"), Integer.valueOf(1));

		s3.cancel();
		ac = pubsub.activeChannels();
		assertEquals(ac.size(), 1);
		assertEquals(ac.get("baz"), Integer.valueOf(1));

		s4.cancel();
		ac = pubsub.activeChannels();
		assertTrue(pubsub.activeChannels().isEmpty());
	}

	private static final class RecordingMessageHandler implements MessageHandler {

		private Map<String, List<Object>> msgs = new HashMap<String, List<Object>>();

		private int numMsgs;

		private final ReentrantLock lock = new ReentrantLock();

		private final Condition newMsg = lock.newCondition();

		@Override
		public void handleMessage(String channelName, Object message) {
			lock.lock();
			try {
				assertNotNull(channelName);
				assertNotNull(message);
				numMsgs++;
				List<Object> l = msgs.get(channelName);
				if (l == null) {
					l = new LinkedList<Object>();
					msgs.put(channelName, l);
				}
				l.add(message);
				newMsg.signalAll();
			} finally {
				lock.unlock();
			}
		}

		public void awaitNumMsgs(int num) throws InterruptedException {
			lock.lock();
			try {
				while (this.numMsgs < num) {
					newMsg.await();
				}
			} finally {
				lock.unlock();
			}
		}

		public void assertMessages(String channelName, Object... messages) throws InterruptedException {
			if (messages != null && messages.length > 0) {
				awaitNumMsgs(messages.length);
			}
			lock.lock();
			try {
				List<Object> l = msgs.get(channelName);
				if (messages == null || messages.length == 0) {
					assertNull(l);
				} else {
					assertNotNull(l);
					assertEquals(l.size(), messages.length);
					assertEquals(l, Arrays.asList(messages));
				}
			} finally {
				lock.unlock();
			}
		}

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}

	private static final class ThrowingMessageHandler implements MessageHandler {

		@Override
		public void handleMessage(String channelName, Object message) {
			throw new RuntimeException("expected unit test exception");
		}
	}

	private static final class FanIn implements MessageHandler {

		private final String fromA;

		private final String fromB;

		private final String to;

		private final PubSub pubsub;

		public FanIn(String fromA, String fromB, String to, PubSub pubsub) {
			this.fromA = fromA;
			this.fromB = fromB;
			this.to = to;
			this.pubsub = pubsub;
			pubsub.subscribe(fromA, this);
			pubsub.subscribe(fromB, this);
		}

		@Override
		public void handleMessage(String channelName, Object message) {
			assertTrue(channelName.equals(fromA) || channelName.equals(fromB));
			pubsub.publish(to, message);
		}
	}

	private static final class FanOut implements MessageHandler {

		private final String from;

		private final String toA;

		private final String toB;

		private final PubSub pubsub;

		public FanOut(String from, String toA, String toB, PubSub pubsub) {
			this.from = from;
			this.toA = toA;
			this.toB = toB;
			this.pubsub = pubsub;
			pubsub.subscribe(from, this);
		}

		@Override
		public void handleMessage(String channelName, Object message) {
			assertEquals(channelName, from);
			pubsub.publish(toA, message);
			pubsub.publish(toB, message);
		}
	}

	private static final class RingNode implements MessageHandler {

		public final AtomicInteger msgCount = new AtomicInteger();
		private final String from;
		private final String to;
		private final PubSub pubsub;

		public RingNode(String from, String to, PubSub pubsub) {
			this.from = from;
			this.to = to;
			this.pubsub = pubsub;
			pubsub.subscribe(from, this);
		}

		@Override
		public void handleMessage(String channelName, Object message) {
			assertEquals(channelName, from);
			assertTrue(message instanceof Long);
			if (!message.equals(0L)) {
				msgCount.incrementAndGet();
				Long next = ((Long) message).longValue() - 1;
				pubsub.publish(to, next);
			}
		}
	}

	private static final class Forwarder implements MessageHandler {

		private final String from;
		private final String to;

		private final PubSub pubsub;

		Forwarder(String from, String to, PubSub pubsub) {
			this.from = from;
			this.to = to;
			this.pubsub = pubsub;
			pubsub.subscribe(from, this);
		}

		@Override
		public void handleMessage(String channelName, Object message) {
			assertEquals(channelName, from);
			pubsub.publish(to, message);
		}
	}
}
