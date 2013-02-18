package ch.bind.philib.lang;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread factory which generates thread names of the form
 * &lt;name&gt;-&lt;sequence&gt;
 * 
 * @author philipp meinen
 */
public final class NamedSeqThreadFactory implements ThreadFactory {

	private final AtomicLong SEQ = new AtomicLong(0);

	private final String name;

	/**
	 * @see NamedSeqThreadFactory
	 * @param name The name which must be used for newly created threads.
	 */
	public NamedSeqThreadFactory(String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable r) {
		String threadname = name + "-" + SEQ.getAndIncrement();
		return new Thread(r, threadname);
	}
}
