package ch.bind.philib.net.context;

import ch.bind.philib.cache.ByteBufferCache;
import ch.bind.philib.net.events.EventDispatcher;
import ch.bind.philib.validation.Validation;

public class NetContextImpl implements NetContext {

	public static final int DEFAULT_BUFFER_SIZE = 8192;

	public static final int DEFAULT_NUM_BUFFERS = 128;

	private final ByteBufferCache bufferCache;

	private final EventDispatcher eventDispatcher;

	public NetContextImpl(ByteBufferCache bufferCache, EventDispatcher eventDispatcher) {
		Validation.notNull(bufferCache);
		Validation.notNull(eventDispatcher);
		this.bufferCache = bufferCache;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public final ByteBufferCache getBufferCache() {
		return bufferCache;
	}

	@Override
	public final EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}
}
