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

package ch.bind.philib.msg.vm;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import ch.bind.philib.TestUtil;
import ch.bind.philib.lang.ExceptionUtil;
import ch.bind.philib.lang.ThreadUtil;
import ch.bind.philib.msg.MessageHandler;

/**
 * @author Philipp Meinen
 */

public class PubSubVMBenchmark implements Runnable {

	private final int consumersPerProducer;

	private final int numThreads;

	private final long runforms = 60000L;

	private final long incrementEvery = 1000L;

	private PubSub pubSub;

	private ExecutorService executorService;

	private List<Thread> publishers = new LinkedList<Thread>();

	private List<Consumer> consumers = new LinkedList<Consumer>();

	public PubSubVMBenchmark(int consumersPerProducer, int numThreads) {
		this.consumersPerProducer = consumersPerProducer;
		this.numThreads = numThreads;
	}

	public static void main(String[] args) {
		System.out.println("threads;publishers;consumers;messages;latency-ns;persec");
		for (int ratio = 1; ratio < 9; ratio *= 2) {
			for (int threads = 1; threads < 9; threads *= 2) {
				new PubSubVMBenchmark(ratio, threads).run();
				TestUtil.gcAndSleep(100);
			}
		}
	}

	private void printStats(long timeMs) {
		long numMessages = 0;
		long totalLatency = 0;
		for (Consumer c : consumers) {
			numMessages += c.numMessages.get();
			totalLatency += c.totalLatency.get();
		}
		long latency = totalLatency / numMessages;
		double persec = numMessages / (timeMs / 1000f);
		System.out.printf("%d;%d;%d;%d;%d;%.3f\n", numThreads, publishers.size(), consumers.size(), numMessages, latency, persec);
	}

	@Override
	public void run() {
		try {
			long numIncrements = runforms / incrementEvery;
			setup();
			final long start = System.currentTimeMillis();
			for (int i = 0; i < numIncrements; i++) {
				create(i);
				long sleepUntil = start + (i + 1) * incrementEvery;
				try {
					ThreadUtil.sleepUntilMs(sleepUntil);
				} catch (InterruptedException e) {
					System.out.println("interrupted " + ExceptionUtil.buildMessageChain(e));
					System.exit(1);
				}
				printStats((i + 1) * incrementEvery);
			}
			stop();
			printStats(runforms);
			publishers.clear();
			consumers.clear();
		} catch (Exception e) {
			System.err.println(ExceptionUtil.buildMessageChain(e));
			System.exit(1);
		}
	}

	private void setup() {
		BlockingQueue<Runnable> q = new LinkedBlockingQueue<Runnable>();
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(numThreads, numThreads, 1L, TimeUnit.SECONDS, q);

		executorService = tpe;
		pubSub = new PubSubVM(executorService);
	}

	private void stop() throws InterruptedException {
		for (Thread t : publishers) {
			if (!ThreadUtil.interruptAndJoin(t)) {
				System.err.println("thread did not stop: " + t);
				System.exit(1);
			}
		}
		executorService.shutdown();
		if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
			System.err.println("executor-service did not terminate within 1 second");
			System.exit(1);
		}
	}

	private void create(int idx) {
		Producer p = new Producer(pubSub, false);
		Thread t = new Thread(p, "publisher-" + idx);
		publishers.add(t);
		t.start();
		for (int i = 0; i < consumersPerProducer; i++) {
			Consumer c = new Consumer(pubSub);
			consumers.add(c);
		}
	}

	private static final class Producer implements Runnable {

		private final PubSub pubSub;

		private final boolean async;

		public Producer(PubSub pubSub, boolean async) {
			super();
			this.pubSub = pubSub;
			this.async = async;
		}

		@Override
		public void run() {
			final Thread t = Thread.currentThread();
			while (!t.isInterrupted()) {
				Long now = System.nanoTime();
				if (async) {
					pubSub.publishAsync("foo", now);
				} else {
					pubSub.publishSync("foo", now);
				}
			}
		}
	}

	private static final class Consumer implements MessageHandler {

		private AtomicLong numMessages = new AtomicLong();

		private AtomicLong totalLatency = new AtomicLong();

		public Consumer(PubSub pubSub) {
			pubSub.subscribe("foo", this);
		}

		@Override
		public void handleMessage(String channelName, Object message) {
			long now = System.nanoTime();
			if (message instanceof Long) {
				Long sentAt = (Long) message;
				long latency = now - sentAt;
				numMessages.incrementAndGet();
				totalLatency.addAndGet(latency);
			}
		}
	}
}
