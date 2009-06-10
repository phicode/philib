package ch.bind.philib.concurrency;

public interface Lockable {

	public long getLockId();

	public void lock();

	public void unlock();

	public boolean trylock();
}
