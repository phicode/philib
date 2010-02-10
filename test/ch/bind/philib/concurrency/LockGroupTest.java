package ch.bind.philib.concurrency;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

import ch.bind.philib.PhiArrays;

public class LockGroupTest {

	// TODO: beautify

	private static final int NUM_LOCKABLES = 1000;
	private static final int NUM_LOCKGROUPS = 25;
	private static final int NUM_LOCKABLE_PER_GROUP = 20;
	private static final int NUM_THREADS = 8;
	private static final int NUM_ITERATIONS_PER_THREAD = 100000;

	private Thread[] threads;
	private Lockable[] lockables;
	private LockGroup[] lockgroups;

	@Test(timeout = 15000)
	public void concurrentTest() throws InterruptedException {
		initThread();
		initLockables();
		initLockgroups();
		for (Thread t : threads)
			t.start();

		for (Thread t : threads) {
			t.join();
		}
	}

	private void initLockgroups() {
		lockgroups = new LockGroup[NUM_LOCKGROUPS];
		Lockable[] lockInGrp = new Lockable[NUM_LOCKABLE_PER_GROUP];
		for (int i = 0; i < NUM_LOCKGROUPS; i++) {
			PhiArrays.pickRandom(lockables, lockInGrp);
			lockgroups[i] = new LockGroup(lockInGrp);
		}
	}

	private void initLockables() {
		lockables = new Lockable[NUM_LOCKABLES];
		for (int i = 0; i < NUM_LOCKABLES; i++) {
			lockables[i] = new LockableStub();
		}
	}

	private static class LockableStub implements Lockable {

		private final long lockId = LockManager.getNextLockId();
		private final Lock lock = new ReentrantLock();

		@Override
		public long getLockId() {
			return lockId;
		}

		@Override
		public void lock() {
			lock.lock();
		}

		@Override
		public boolean trylock() {
			return lock.tryLock();
		}

		@Override
		public void unlock() {
			lock.unlock();
		}

	}

	private void initThread() {
		threads = new Thread[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i] = new Thread(new RandomLocker(), "RandomLocker"
					+ (i + 1));
		}
	}

	private class RandomLocker implements Runnable {

		private final Random rand = new Random();

		@Override
		public void run() {
			final int iter = NUM_ITERATIONS_PER_THREAD;
			for (int i = 0; i < iter; i++) {
				int idx = rand.nextInt(NUM_LOCKGROUPS);
				LockGroup lg = lockgroups[idx];
				lg.lock();
				Thread.yield();
				lg.unlock();
			}
		}
	}

	@Test(timeout = 5000)
	public void testWithProphets() throws InterruptedException {
		Thread[] ts = new Thread[5];
		Lockable[] las = new Lockable[5];
		LockGroup[] lgs = new LockGroup[5];

		for (int i = 0; i < 5; i++) {
			las[i] = new LockableStub();
		}

		for (int i = 0; i < 5; i++) {
			Lockable[] lgslocks = new Lockable[2];
			lgslocks[0] = las[i];
			lgslocks[1] = las[(i + 1) % 5];
			lgs[i] = new LockGroup(lgslocks);
		}

		for (int i = 0; i < 5; i++) {
			ts[i] = new Thread(new Locker(lgs[i]));
		}

		for (Thread t : ts)
			t.start();
		for (Thread t : ts)
			t.join();
	}

	private static class Locker implements Runnable {

		private LockGroup lg;

		private Locker(LockGroup lg) {
			this.lg = lg;
		}

		@Override
		public void run() {
			for (int i = 0; i < NUM_ITERATIONS_PER_THREAD; i++) {
				lg.lock();
				Thread.yield();
				lg.unlock();
			}
		}

	}
}
