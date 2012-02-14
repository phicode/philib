package ch.bind.philib.net;

import java.util.concurrent.Semaphore;

import ch.bind.philib.io.NQueue;
import ch.bind.philib.validation.SimpleValidation;

public final class NetQ<E> {

	private final NQueue<E> out;

	private final NQueue<E> in;

	private NetQ(Semaphore inSem, Semaphore outSem) {
		out = new NQueue<E>(inSem);
		in = new NQueue<E>(outSem);
	}

	public static <E> NetQ<E> create(Semaphore inSem, Semaphore outSem) {
		SimpleValidation.notNull(inSem);
		SimpleValidation.notNull(outSem);
		return new NetQ<E>(inSem, outSem);
	}

	public NQueue<E> getOut() {
		return out;
	}

	public NQueue<E> getIn() {
		return in;
	}
}
