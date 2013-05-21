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
		assertTrue(runnables.isEmpty());
	}

	@Test(timeOut = 500)
	public void syncEmptyPublish() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		pubsub.publishSync("foo", "bar");
	}

	@Test(timeOut = 500)
	public void asyncEmptyPublish() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		pubsub.publishAsync("foo", "bar");
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
	public void syncMessages() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		Subscription s = pubsub.subscribe("foo", mh);
		assertEquals(s.getChannelName(), "foo");
		pubsub.publishSync("foo", "bar");
		pubsub.publishSync("foo", "baz");
		mh.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void asyncMessages() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh);
		pubsub.publishAsync("foo", "bar");
		pubsub.publishAsync("foo", "baz");
		mh.awaitNumMsgs(2);
		mh.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void syncMessagesTwoSubscribers() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh1 = new RecordingMessageHandler();
		RecordingMessageHandler mh2 = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh1);
		pubsub.subscribe("foo", mh2);
		pubsub.publishSync("foo", "bar");
		pubsub.publishSync("foo", "baz");
		mh1.assertMessages("foo", "bar", "baz");
		mh2.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void asyncMessagesTwoSubscribers() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh1 = new RecordingMessageHandler();
		RecordingMessageHandler mh2 = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh1);
		pubsub.subscribe("foo", mh2);
		pubsub.publishAsync("foo", "bar");
		pubsub.publishAsync("foo", "baz");
		mh1.awaitNumMsgs(2);
		mh2.awaitNumMsgs(2);
		mh1.assertMessages("foo", "bar", "baz");
		mh2.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void unsubscribe() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh1 = new RecordingMessageHandler();
		RecordingMessageHandler mh2 = new RecordingMessageHandler();
		Subscription s1 = pubsub.subscribe("foo", mh1);
		Subscription s2 = pubsub.subscribe("foo", mh2);
		assertNotNull(s1);
		assertNotNull(s2);
		assertTrue(s1.isActive() && s2.isActive());
		pubsub.publishSync("foo", "bar");
		s1.cancel();
		assertTrue(!s1.isActive() && s2.isActive());
		pubsub.publishSync("foo", "baz");
		s2.cancel();
		assertTrue(!s1.isActive() && !s2.isActive());
		pubsub.publishSync("foo", "xxx");
		mh1.assertMessages("foo", "bar");
		mh2.assertMessages("foo", "bar", "baz");
		s2.cancel(); // noop
	}

	@Test(timeOut = 500)
	public void noDeliveryOnOtherChannels() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh);
		pubsub.publishSync("foo", "bar");
		pubsub.publishSync("yyy", "zzz");
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
		pubsub.publishAsync("foo", "bar");

		// wait for the async publisher to start working
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
		active.awaitNumMsgs(1);
		active.assertMessages("foo", "bar");
		cancel.assertMessages("foo");
	}

	@Test(timeOut = 500)
	public void dontFailOnSubscriberException() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("foo", new ThrowingMessageHandler());
		pubsub.subscribe("foo", mh);
		pubsub.publishSync("foo", "bar");
		mh.assertMessages("foo", "bar");
	}

	@Test(timeOut = 5000)
	public void simpleFanInFanOut() {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		boolean sync = true;
		FanOut root = new FanOut("root", "1.1", "1.2", pubsub, sync);
		FanOut out1 = new FanOut("1.1", "2.1", "2.2", pubsub, sync);
		FanOut out2 = new FanOut("1.2", "2.2", "2.3", pubsub, sync);
		FanIn in1 = new FanIn("2.1", "2.2", "3.1", pubsub, sync);
		FanIn in2 = new FanIn("2.2", "2.3", "3.2", pubsub, sync);
		FanIn end = new FanIn("3.1", "3.2", "end", pubsub, sync);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("end", mh);
		pubsub.publishSync("root", "bar");
		assertEquals(in1.numRecv.get(), 3);
		assertEquals(in2.numRecv.get(), 3);
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
		pubsub.publishSync("0", "bar");
		tail.assertMessages("1000", "bar");
		pubsub.publishAsync("0", "baz");
		tail.awaitNumMsgs(2);
		tail.assertMessages("1000", "bar", "baz");
	}

	// TODO: make sure that sync rings are broken up through eventual async
	// calls
	// @Test(timeOut = 5000)
	// public void syncRing() throws InterruptedException {
	// PubSub pubsub = new PubSubVM(singleThreadExecutor);
	// boolean sync = true;
	// for (int i = 0; i < 1000; i++) {
	// String from = Integer.toString(i);
	// String to = i == 999 ? "0" : Integer.toString(i + 1);
	// new RingNode(from, to, pubsub, sync);
	// }
	// pubsub.publishSync("1", 100000L);
	// }

	@Test(timeOut = 5000)
	public void asyncRing() throws InterruptedException {
		PubSub pubsub = new PubSubVM(singleThreadExecutor);
		boolean sync = false;
		for (int i = 0; i < 1000; i++) {
			String from = Integer.toString(i);
			String to = i == 999 ? "0" : Integer.toString(i + 1);
			new RingNode(from, to, pubsub, sync);
		}
		pubsub.publishAsync("1", 100000L);
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

		public void assertMessages(String channelName, Object... messages) {
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

		private final boolean sync;

		private final AtomicInteger numRecv = new AtomicInteger();

		public FanIn(String fromA, String fromB, String to, PubSub pubsub, boolean sync) {
			this.fromA = fromA;
			this.fromB = fromB;
			this.to = to;
			this.pubsub = pubsub;
			this.sync = sync;
			pubsub.subscribe(fromA, this);
			pubsub.subscribe(fromB, this);
		}

		@Override
		public void handleMessage(String channelName, Object message) {
			assertTrue(channelName.equals(fromA) || channelName.equals(fromB));
			numRecv.incrementAndGet();
			if (sync) {
				pubsub.publishSync(to, message);
			} else {
				pubsub.publishAsync(to, message);
			}
		}
	}

	private static final class FanOut implements MessageHandler {

		private final String from;

		private final String toA;

		private final String toB;

		private final PubSub pubsub;

		private final boolean sync;

		public FanOut(String from, String toA, String toB, PubSub pubsub, boolean sync) {
			this.from = from;
			this.toA = toA;
			this.toB = toB;
			this.pubsub = pubsub;
			this.sync = sync;
			pubsub.subscribe(from, this);
		}

		@Override
		public void handleMessage(String channelName, Object message) {
			assertEquals(channelName, from);
			if (sync) {
				pubsub.publishSync(toA, message);
				pubsub.publishSync(toB, message);
			} else {
				pubsub.publishAsync(toA, message);
				pubsub.publishAsync(toB, message);
			}
		}
	}

	private static final class RingNode implements MessageHandler {

		private final String from;
		private final String to;
		private final PubSub pubsub;
		private final boolean sync;

		public RingNode(String from, String to, PubSub pubsub, boolean sync) {
			this.from = from;
			this.to = to;
			this.pubsub = pubsub;
			this.sync = sync;
			pubsub.subscribe(from, this);
		}

		@Override
		public void handleMessage(String channelName, Object message) {
			assertEquals(channelName, from);
			assertTrue(message instanceof Long);
			if (!message.equals(0L)) {
				Long next = ((Long) message).longValue() - 1;
				if (sync) {
					pubsub.publishSync(to, next);
				} else {
					pubsub.publishAsync(to, next);
				}
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
			pubsub.publishSync(to, message);
		}
	}
}
