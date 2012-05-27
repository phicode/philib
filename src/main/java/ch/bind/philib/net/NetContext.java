package ch.bind.philib.net;

import java.io.IOException;

import ch.bind.philib.cache.ByteBufferCache;
import ch.bind.philib.net.sel.NetSelector;
import ch.bind.philib.net.sel.SimpleNetSelector;
import ch.bind.philib.validation.SimpleValidation;

public final class NetContext {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private static final int DEFAULT_NUM_BUFFERS = 32;

	private final ByteBufferCache bufferCache;

	private final NetSelector netSelector;

	public NetContext(ByteBufferCache bufferCache, NetSelector netSelector) {
		SimpleValidation.notNull(bufferCache);
		SimpleValidation.notNull(netSelector);
		this.bufferCache = bufferCache;
		this.netSelector = netSelector;
	}

	public ByteBufferCache getBufferCache() {
		return bufferCache;
	}

	public NetSelector getNetSelector() {
		return netSelector;
	}

	/**
	 * 
	 * @return
	 * @throws IOException In case the selector creation failed.
	 */
	public static NetContext createDefault() throws IOException {
		NetSelector netSelector = SimpleNetSelector.open();
		ByteBufferCache bufferCache = ByteBufferCache.createScalable(DEFAULT_BUFFER_SIZE, DEFAULT_NUM_BUFFERS);
		return new NetContext(bufferCache, netSelector);
	}
}
