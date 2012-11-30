package ch.bind.philib.net.context;

import java.io.IOException;

import ch.bind.philib.net.SessionFactory;
import ch.bind.philib.net.events.EventDispatcher;
import ch.bind.philib.net.events.SimpleEventDispatcher;
import ch.bind.philib.pool.buffer.ByteBufferPool;
import ch.bind.philib.validation.Validation;

public final class NetContexts {

	private NetContexts() {
	}

	public static NetContext createSimple(SessionFactory sessionFactory) throws IOException {
		Validation.notNull(sessionFactory);
		int bufferSize = NetContextImpl.DEFAULT_BUFFER_SIZE;
		int maxEntries = NetContextImpl.DEFAULT_NUM_BUFFERS;
		ByteBufferPool pool = ByteBufferPool.create(bufferSize, maxEntries);
		EventDispatcher dispatcher = SimpleEventDispatcher.open();
		return new NetContextImpl(sessionFactory, pool, dispatcher);
	}
}
