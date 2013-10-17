package ch.bind.philib.exp.chan;

import java.io.Closeable;
import java.io.IOException;

public final class Channel implements RxChannel, TxChannel, Closeable {

	public static final int DEFAULT_CAPACITY = 16;

	private final Object lock = new Object();

	private final Object[] content;

	private int rpos = -1;

	private int wpos = -1;

	public Channel() {
		this(DEFAULT_CAPACITY);
	}

	public Channel(int capacity) {
		this.content = new Object[capacity];
	}

	@Override
	public boolean trySend(Object message) {
		// TODO Auto-generated method stub
		return false;
	}

	public void send(Object message) {
		if (message == null) {
			return;
		}
		synchronized(lock) {
			if (wpos == -1) {
				wpos = 0;
				content[0] =message;
				lock.notify();
				return;
			}
			while(true) {
				int insertPos = (wpos+1)%content.length;
//				if (content[insertPos])
			}
		}
	}

	public Object receive() {
		return null;
	}

	public Object poll() {
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
	}

	public RxChannel getRxChannel() {
		return new RxC();
	}

	public TxChannel getTxChannel() {
		return new TxC();
	}

	private final class RxC implements RxChannel {

		@Override
		public Object receive() {
			return Channel.this.receive();
		}

		@Override
		public Object poll() {
			return Channel.this.poll();
		}
	}

	private final class TxC implements TxChannel {

		@Override
		public void close() throws IOException {
			Channel.this.close();
		}

		@Override
		public boolean trySend(Object message) {
			return Channel.this.trySend(message);
		}

		@Override
		public void send(Object message) {
			Channel.this.send(message);
		}
	}
}
