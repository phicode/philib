package ch.bind.philib.msg;

import static org.testng.Assert.assertEquals;
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

public class DefaultTinyPubSubTest {

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
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		pubsub.publishSync("foo", "bar");
	}

	@Test(timeOut = 500)
	public void asyncEmptyPublish() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		pubsub.publishAsync("foo", "bar");
	}

	@Test(timeOut = 500)
	public void syncDeadLetterPublish() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler dlh = new RecordingMessageHandler();
		pubsub.addDeadLetterHandler(dlh);
		pubsub.publishSync("foo", "bar");
		dlh.assertMessages("foo", "bar");
	}

	@Test(timeOut = 500)
	public void asyncDeadLetterPublish() throws InterruptedException {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler dlh = new RecordingMessageHandler();
		pubsub.addDeadLetterHandler(dlh);
		pubsub.publishAsync("foo", "bar");
		dlh.awaitNumMsgs(1);
		dlh.assertMessages("foo", "bar");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void noNullSubscribeChannelName() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe(null, mh);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void noEmptySubscribeChannelName() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("", mh);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void noNullSubscribeMessageHandler() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		pubsub.subscribe("", null);
	}

	@Test(timeOut = 500)
	public void syncMessages() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		Subscription s = pubsub.subscribe("foo", mh);
		assertEquals(s.getChannelName(), "foo");
		pubsub.publishSync("foo", "bar");
		pubsub.publishSync("foo", "baz");
		mh.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void asyncMessages() throws InterruptedException {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh);
		pubsub.publishAsync("foo", "bar");
		pubsub.publishAsync("foo", "baz");
		mh.awaitNumMsgs(2);
		mh.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void syncMessagesTwoSubscribers() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
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
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
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
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler mh1 = new RecordingMessageHandler();
		RecordingMessageHandler mh2 = new RecordingMessageHandler();
		RecordingMessageHandler dlh = new RecordingMessageHandler();
		Subscription s1 = pubsub.subscribe("foo", mh1);
		Subscription s2 = pubsub.subscribe("foo", mh2);
		pubsub.addDeadLetterHandler(dlh);
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
		dlh.assertMessages("foo", "xxx");
		s2.cancel(); // noop
	}

	@Test(timeOut = 500)
	public void noDeliveryOnOtherChannels() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh);
		pubsub.publishSync("foo", "bar");
		pubsub.publishSync("yyy", "zzz");
		mh.assertMessages("foo", "bar");
	}

	@Test(timeOut = 500)
	public void noDeliveryOnRemovedDeadLetterHandler() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler dlh1 = new RecordingMessageHandler();
		RecordingMessageHandler dlh2 = new RecordingMessageHandler();
		pubsub.addDeadLetterHandler(dlh1);
		pubsub.addDeadLetterHandler(dlh2);

		pubsub.publishSync("foo", "bar");
		dlh1.assertMessages("foo", "bar");
		dlh2.assertMessages("foo", "bar");

		pubsub.removeDeadLetterHandler(dlh1);
		pubsub.publishSync("foo", "baz");
		dlh1.assertMessages("foo", "bar");
		dlh2.assertMessages("foo", "bar", "baz");

		pubsub.removeDeadLetterHandler(dlh2);
		pubsub.publishSync("foo", "yyy");
		dlh1.assertMessages("foo", "bar");
		dlh2.assertMessages("foo", "bar", "baz");
	}

	@Test(timeOut = 500)
	public void unsubscribeWhilePublishingIsRunning() throws InterruptedException {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler mh1 = new RecordingMessageHandler();
		RecordingMessageHandler mh2 = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh1);
		Subscription s = pubsub.subscribe("foo", mh2);

		// lock the first message-handler so that the notification publisher can
		// not proceed
		mh1.lock.lock();
		pubsub.publishAsync("foo", "bar");

		// wait for the async publisher to start working
		while (!mh1.lock.hasQueuedThreads()) {
			Thread.yield();
		}
		// now cancel the second key and make the publisher unstuck
		s.cancel();

		mh1.assertMessages("foo");
		mh1.lock.unlock();
		mh1.awaitNumMsgs(1);
		mh1.assertMessages("foo", "bar");
		mh2.assertMessages("foo");
	}

	@Test(timeOut = 500)
	public void deadLetterIfMessageUnhandled() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		RecordingMessageHandler dlh = new RecordingMessageHandler();
		mh.doHandleMessages = false;
		pubsub.subscribe("foo", mh);
		pubsub.addDeadLetterHandler(dlh);
		pubsub.publishSync("foo", "bar");
		mh.assertMessages("foo");
		dlh.assertMessages("foo", "bar");
	}

	@Test(timeOut = 500)
	public void deadLetterIfMessageUnhandledAsync() throws InterruptedException {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		RecordingMessageHandler mh = new RecordingMessageHandler();
		RecordingMessageHandler dlh = new RecordingMessageHandler();
		mh.doHandleMessages = false;
		pubsub.subscribe("foo", mh);
		pubsub.addDeadLetterHandler(dlh);
		pubsub.publishAsync("foo", "bar");
		dlh.awaitNumMsgs(1);
		mh.assertMessages("foo");
		dlh.assertMessages("foo", "bar");
	}

	@Test(timeOut = 500)
	public void dontFailOnSubscriberException() throws InterruptedException {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		MessageHandler mh = new ThrowingMessageHandler();
		RecordingMessageHandler dlh = new RecordingMessageHandler();
		pubsub.subscribe("foo", mh);
		pubsub.addDeadLetterHandler(dlh);
		pubsub.publishSync("foo", "bar");
		dlh.awaitNumMsgs(1);
		dlh.assertMessages("foo", "bar");
	}

	@Test(timeOut = 500)
	public void dontFailOnDeadLetterException() throws InterruptedException {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		MessageHandler dlh1 = new ThrowingMessageHandler();
		RecordingMessageHandler dlh2 = new RecordingMessageHandler();
		pubsub.addDeadLetterHandler(dlh1);
		pubsub.addDeadLetterHandler(dlh2);
		pubsub.publishSync("foo", "bar");
		dlh2.assertMessages("foo", "bar");
	}

	// @Test(timeOut = 5000)
	// public void fanInFanOut() {
	// TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
	// boolean sync = true;
	// int levels = 5;
	// FanOut root = new FanOut("root", "1.1", "1.2", pubsub, sync);
	// List<FanOut> fo = new LinkedList<FanOut>();
	// fo.add(root);
	// for (int l = 0; l < levels; l++) {
	//
	// }
	// }

	@Test(timeOut = 5000)
	public void simpleFanInFanOut() {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
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

	// TODO: ring
	@Test(timeOut = 5000)
	public void simpleChain() throws InterruptedException {
		TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
		pubsub.forward("0", "1");
		RecordingMessageHandler tail = new RecordingMessageHandler();
		pubsub.subscribe("1000", tail);
		for (int i = 1; i < 1000; i++) {
			pubsub.forward(Integer.toString(i), Integer.toString(i + 1));
		}
		pubsub.publishSync("0", "bar");
		tail.assertMessages("1000", "bar");
		pubsub.publishAsync("0", "baz");
		tail.awaitNumMsgs(2);
		tail.assertMessages("1000", "bar", "baz");
	}

	// @Test(invocationCount = 1)
	// public void longSyncChain() {
	// TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
	// boolean sync = true;
	// Forwarder head = new Forwarder("0", "1", pubsub, sync);
	// RecordingMessageHandler tail = new RecordingMessageHandler();
	// pubsub.subscribe("1000000", tail);
	// for (int i = 1; i < 1000000; i++) {
	// new Forwarder(Integer.toString(i), Integer.toString(i + 1), pubsub,
	// sync);
	// }
	// pubsub.publishSync("0", "bar");
	// tail.assertMessages("1000000", "bar");
	// }

	// TODO: ring
	// @Test(timeOut=10000)
	// public void longAsyncChain() throws InterruptedException {
	// TinyPubSub pubsub = new DefaultTinyPubSub(singleThreadExecutor);
	// pubsub.forward("0", "1");
	// RecordingMessageHandler tail = new RecordingMessageHandler();
	// pubsub.subscribe("10000", tail);
	// for (int i = 1; i < 1000000; i++) {
	// pubsub.forward(Integer.toString(i), Integer.toString(i + 1));
	// }
	// pubsub.publishAsync("0", "bar");
	// tail.awaitNumMsgs(1);
	// tail.assertMessages("100000", "bar");
	// }

	private static final class RecordingMessageHandler implements MessageHandler {

		private Map<String, List<Object>> msgs = new HashMap<String, List<Object>>();

		private int numMsgs;

		private boolean doHandleMessages = true;

		private final ReentrantLock lock = new ReentrantLock();

		private final Condition newMsg = lock.newCondition();

		@Override
		public boolean handleMessage(String channelName, Object message) {
			lock.lock();
			try {
				assertNotNull(channelName);
				assertNotNull(message);
				if (doHandleMessages) {
					numMsgs++;
					List<Object> l = msgs.get(channelName);
					if (l == null) {
						l = new LinkedList<Object>();
						msgs.put(channelName, l);
					}
					l.add(message);
					newMsg.signalAll();
				}
				return doHandleMessages;
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
				}
				else {
					assertNotNull(l);
					assertEquals(l.size(), messages.length);
					assertEquals(l, Arrays.asList(messages));
				}
			} finally {
				lock.unlock();
			}
		}
	}

	private static final class ThrowingMessageHandler implements MessageHandler {

		@Override
		public boolean handleMessage(String channelName, Object message) {
			throw new RuntimeException("some people actually dont protect their code ... so we will do that for them");
		}
	}

	private static final class FanIn implements MessageHandler {

		private final String fromA;

		private final String fromB;

		private final String to;

		private final TinyPubSub pubsub;

		private final boolean sync;

		private final AtomicInteger numRecv = new AtomicInteger();

		public FanIn(String fromA, String fromB, String to, TinyPubSub pubsub, boolean sync) {
			this.fromA = fromA;
			this.fromB = fromB;
			this.to = to;
			this.pubsub = pubsub;
			this.sync = sync;
			pubsub.subscribe(fromA, this);
			pubsub.subscribe(fromB, this);
		}

		@Override
		public boolean handleMessage(String channelName, Object message) {
			assertTrue(channelName.equals(fromA) || channelName.equals(fromB));
			numRecv.incrementAndGet();
			if (sync) {
				pubsub.publishSync(to, message);
			}
			else {
				pubsub.publishAsync(to, message);
			}
			return true;
		}
	}

	private static final class FanOut implements MessageHandler {

		private final String from;

		private final String toA;

		private final String toB;

		private final TinyPubSub pubsub;

		private final boolean sync;

		public FanOut(String from, String toA, String toB, TinyPubSub pubsub, boolean sync) {
			this.from = from;
			this.toA = toA;
			this.toB = toB;
			this.pubsub = pubsub;
			this.sync = sync;
			pubsub.subscribe(from, this);
		}

		@Override
		public boolean handleMessage(String channelName, Object message) {
			assertEquals(channelName, from);
			if (sync) {
				pubsub.publishSync(toA, message);
				pubsub.publishSync(toB, message);
			}
			else {
				pubsub.publishAsync(toA, message);
				pubsub.publishAsync(toB, message);
			}
			return true;
		}
	}
}
