package ch.bind.philib.net.events;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import ch.bind.philib.net.events.ConcurrentEventDispatcher.ScaleStrategy;
import static org.testng.Assert.*;

public class ConcurrentEventDispatcherTest {

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "duplicate registration for event with id=0 : .*")
	public void noDuplicateIds() {
		RecordingEventDispatcher[] disps = new RecordingEventDispatcher[2];
		disps[0] = new RecordingEventDispatcher();
		disps[1] = new RecordingEventDispatcher();
		ConcurrentEventDispatcher concDisp = new ConcurrentEventDispatcher(disps, ScaleStrategy.ROUND_ROBIN);

		concDisp.register(new RecordingEventHandler(0), 0);
		concDisp.register(new RecordingEventHandler(0), 0);
	}

	static class RecordingEventHandler implements EventHandler {

		final long id;

		public RecordingEventHandler(long id) {
			this.id = id;
		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public SelectableChannel getChannel() {
			return null;
		}

		@Override
		public int handleOps(int ops) throws IOException {
			return 0;
		}

		@Override
		public boolean handleTimeout() throws IOException {
			return false;
		}

		@Override
		public long getEventHandlerId() {
			return 0;
		}
	}

	static class RecordingEventDispatcher implements EventDispatcher {

		boolean open = true;

		Map<Long, EventHandler> map = new HashMap<Long, EventHandler>();

		@Override
		public void close() throws IOException {
			assertTrue(open);
			open = false;
		}

		@Override
		public boolean isOpen() {
			return open;
		}

		@Override
		public void register(EventHandler eventHandler, int ops) {
			assertNotNull(eventHandler);
			EventHandler oldHandler = map.put(eventHandler.getEventHandlerId(), eventHandler);
			assertNull(oldHandler);
		}

		@Override
		public void unregister(EventHandler eventHandler) {
			assertNotNull(eventHandler);
			EventHandler handler = map.remove(eventHandler.getEventHandlerId());
			assertTrue(handler == eventHandler);
		}

		@Override
		public void setTimeout(EventHandler eventHandler, long timeout, TimeUnit timeUnit) {
			assertNotNull(eventHandler);
			assertNotNull(timeUnit);
		}

		@Override
		public void unsetTimeout(EventHandler eventHandler) {
			assertNotNull(eventHandler);
		}

		@Override
		public int getRegisteredOps(EventHandler eventHandler) {
			assertNotNull(eventHandler);
			return 0;
		}

		@Override
		public int getNumEventHandlers() {
			return map.size();
		}

		@Override
		public long getLoadAvg() {
			return 0;
		}
	}
}
