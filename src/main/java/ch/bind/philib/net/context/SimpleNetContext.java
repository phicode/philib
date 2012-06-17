package ch.bind.philib.net.context;

import java.io.IOException;

import ch.bind.philib.cache.ByteBufferCache;
import ch.bind.philib.net.events.SimpleEventDispatcher;

public final class SimpleNetContext extends NetContextImpl implements NetContext {

    public SimpleNetContext() throws IOException {
        // single threaded net selector and buffer cache
        super(ByteBufferCache.createSimple(DEFAULT_BUFFER_SIZE, DEFAULT_NUM_BUFFERS), //
                SimpleEventDispatcher.open());
    }
}
