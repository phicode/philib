package ch.bind.philib.util;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.testng.annotations.Test;

import ch.bind.philib.util.MultiQueue.Sub;

public class MultiQueueTest {

	@Test
	public void multiq() throws Exception {
		qtest(1, 100, 100);
		qtest(1, 1000, 1000);
		qtest(1, 10000, 10000);
		qtest(10, 10000, 10000);
		qtest(100, 10000, 10000);
		qtest(1000, 10000, 10000);
		qtest(10000, 10000, 10000);
	}

	@Test
	public void latency() throws Exception {
		qtest(1, 100000, 1);
		qtest(1, 100000, 10);
		qtest(1, 100000, 100);

		qtest(10, 100000, 1);
		qtest(10, 100000, 10);
		qtest(10, 100000, 100);

		qtest(100, 10000, 1);
		qtest(100, 10000, 10);
		qtest(100, 10000, 100);

		qtest(1000, 10000, 1);
		qtest(1000, 10000, 10);
		qtest(1000, 10000, 100);

		qtest(10000, 1000, 1);
		qtest(10000, 1000, 10);
		qtest(10000, 1000, 100);
	}

	private void qtest(int nsubs, int nmsgs, int burst) throws Exception {
		MultiQueue<Long> q = new MultiQueue<Long>();
		Queue<Counter> results = new ConcurrentLinkedQueue<Counter>();
		CountDownLatch done = new CountDownLatch(nsubs);

		for (int i = 0; i < nsubs; i++) {
			Sub<Long> sub = q.subscribe();
			Runnable r = new QConsumer(results, sub, done);
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.start();
		}

		for (int i = 0; i < nmsgs;) {
			for (int j = 0; j < burst; i++, j++) {
				q.publish(System.nanoTime());
			}
			while (q.headCount() != nsubs) {
				Thread.yield();
			}
		}

		q.close();
		done.await();

		Counter all = new Counter("all");
		for (int i = 0; i < nsubs; i++) {
			Counter c = results.poll();
			assertNotNull(c);
			all.count(c);
		}
		assertNull(results.poll());
		System.out.printf("burst: %d, subscribers: %d msgs: %d total-msgs: %d counter: %s\n", //
				burst, nsubs, nmsgs, nsubs * nmsgs, all);
	}

	private static final class QConsumer implements Runnable {

		private final Queue<Counter> result;

		private final Sub<Long> sub;

		private final CountDownLatch done;

		public QConsumer(Queue<Counter> result, Sub<Long> sub, CountDownLatch done) {
			this.result = result;
			this.sub = sub;
			this.done = done;
		}

		@Override
		public void run() {
			Counter lats = new Counter("latencies");
			Long value = null;
			while ((value = sub.poll()) != null) {
				long lat = System.nanoTime() - value.longValue();
				lats.count(lat);
			}
			assertTrue(result.offer(lats));
			done.countDown();
		}
	}
}
