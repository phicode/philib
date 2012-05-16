package ch.bind.philib.net;

import java.io.IOException;

import ch.bind.philib.net.impl.SimpleNetSelector;
import ch.bind.philib.net.sel.NetSelector;
import ch.bind.philib.pool.ByteBufferPool;
import ch.bind.philib.validation.SimpleValidation;

public final class NetContext {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private static final int DEFAULT_NUM_BUFFERS = 32;

	private final ByteBufferPool bufferPool;

	private final NetSelector netSelector;

	public NetContext(ByteBufferPool bufferPool, NetSelector netSelector) {
		SimpleValidation.notNull(bufferPool);
		SimpleValidation.notNull(netSelector);
		this.bufferPool = bufferPool;
		this.netSelector = netSelector;
	}

	public ByteBufferPool getBufferPool() {
		return bufferPool;
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
		ByteBufferPool bufferPool = ByteBufferPool.createScalable(DEFAULT_BUFFER_SIZE, DEFAULT_NUM_BUFFERS);
		return new NetContext(bufferPool, netSelector);
	}
}
