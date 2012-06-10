package ch.bind.philib.net;

import java.io.IOException;

import ch.bind.philib.cache.ByteBufferCache;
import ch.bind.philib.net.events.EventDispatcher;
import ch.bind.philib.net.events.SimpleEventDispatcher;
import ch.bind.philib.validation.Validation;

public final class NetContext {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private static final int DEFAULT_NUM_BUFFERS = 128;

	private final ByteBufferCache bufferCache;

	private final EventDispatcher netSelector;

	public NetContext(ByteBufferCache bufferCache, EventDispatcher netSelector) {
		Validation.notNull(bufferCache);
		Validation.notNull(netSelector);
		this.bufferCache = bufferCache;
		this.netSelector = netSelector;
	}

	public ByteBufferCache getBufferCache() {
		return bufferCache;
	}

	public EventDispatcher getNetSelector() {
		return netSelector;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 *             In case the selector creation failed.
	 */
	public static NetContext createSimple() throws IOException {
		// single threaded net selector and buffer cache
		EventDispatcher dispatcher = SimpleEventDispatcher.open();
		ByteBufferCache bufferCache = ByteBufferCache.createSimple(DEFAULT_BUFFER_SIZE, DEFAULT_NUM_BUFFERS);
		return new NetContext(bufferCache, dispatcher);
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 *             In case the selector creation failed.
	 */
	public static NetContext createScalable() throws IOException {
		// multi threaded net selector and buffer cache
		throw new UnsupportedOperationException("TODO: scalable net selector");
		// NetSelector netSelector = SimpleNetSelector.open();
		// ByteBufferCache bufferCache =
		// ByteBufferCache.createScalable(DEFAULT_BUFFER_SIZE,
		// DEFAULT_NUM_BUFFERS);
		// return new NetContext(bufferCache, netSelector);
	}
}
