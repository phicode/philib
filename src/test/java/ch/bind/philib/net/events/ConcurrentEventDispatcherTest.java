package ch.bind.philib.net.events;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import ch.bind.philib.net.events.ConcurrentEventDispatcher.ScaleStrategy;
import static org.testng.Assert.*;

public class ConcurrentEventDispatcherTest {

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "duplicate registration for event with id=0 : .*")
	public void noDuplicateIds() throws IOException {
		RecordingEventDispatcher[] disps = new RecordingEventDispatcher[2];
		disps[0] = new RecordingEventDispatcher();
		disps[1] = new RecordingEventDispatcher();
		ConcurrentEventDispatcher concDisp = new ConcurrentEventDispatcher(disps, ScaleStrategy.ROUND_ROBIN);

		concDisp.register(new RecordingEventHandler(0), 0);
		concDisp.register(new RecordingEventHandler(0), 0);
		concDisp.close();
	}

	@Test
	public void multiUnregister() throws IOException {
		RecordingEventDispatcher[] disps = new RecordingEventDispatcher[1];
		disps[0] = new RecordingEventDispatcher();
		ConcurrentEventDispatcher concDisp = new ConcurrentEventDispatcher(disps, ScaleStrategy.ROUND_ROBIN);

		RecordingEventHandler eh = new RecordingEventHandler(0);
		concDisp.register(eh, 0);
		concDisp.unregister(eh);

		// wont throw because event-handler id 0 is no longer registered
		concDisp.register(eh, 0);
		// multiple unregisters also dont throw
		concDisp.unregister(eh);
		concDisp.unregister(eh);
		concDisp.close();

		assertEquals(disps[0].registerCalls, 2);
		assertEquals(disps[0].unregisterCalls, 2);
	}

	@Test
	public void propagateOperations() throws IOException {
		RecordingEventDispatcher[] disps = new RecordingEventDispatcher[1];
		disps[0] = new RecordingEventDispatcher();
		ConcurrentEventDispatcher concDisp = new ConcurrentEventDispatcher(disps, ScaleStrategy.ROUND_ROBIN);

		RecordingEventHandler eh = new RecordingEventHandler(0);
		concDisp.register(eh, 0);
		concDisp.setTimeout(eh, 123, TimeUnit.MILLISECONDS);
		concDisp.unsetTimeout(eh);
		concDisp.setTimeout(eh, 234, TimeUnit.MILLISECONDS);
		concDisp.getNumEventHandlers();
		concDisp.getRegisteredOps(eh);
		concDisp.unregister(eh);

		concDisp.close();

		assertEquals(disps[0].registerCalls, 1);
		assertEquals(disps[0].unregisterCalls, 1);
		assertEquals(disps[0].setTimeoutCalls, 2);
		assertEquals(disps[0].unsetTimeoutCalls, 1);
		assertEquals(disps[0].getNumEventHandlersCalls, 1);
		assertEquals(disps[0].getRegisteredOpsCalls, 1);
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "event handler not registered, id=45 : .*")
	public void throwOnUnknownEventHandler() throws IOException {
		RecordingEventDispatcher[] disps = new RecordingEventDispatcher[1];
		disps[0] = new RecordingEventDispatcher();
		ConcurrentEventDispatcher concDisp = new ConcurrentEventDispatcher(disps, ScaleStrategy.ROUND_ROBIN);
		RecordingEventHandler eh = new RecordingEventHandler(45);
		concDisp.unsetTimeout(eh);
		concDisp.close();
	}

	@Test
	public void closeSubDispatchersOnClose() throws IOException {
		RecordingEventDispatcher[] disps = new RecordingEventDispatcher[2];
		disps[0] = new RecordingEventDispatcher();
		disps[1] = new RecordingEventDispatcher();
		ConcurrentEventDispatcher concDisp = new ConcurrentEventDispatcher(disps, ScaleStrategy.ROUND_ROBIN);

		concDisp.register(new RecordingEventHandler(0), 0);
		concDisp.register(new RecordingEventHandler(1), 0);

		concDisp.close();

		assertFalse(concDisp.isOpen());
		assertEquals(disps[0].closeCalls, 1);
		assertEquals(disps[1].closeCalls, 1);
	}

	@Test
	public void scaleRoundRobin() throws IOException {
		RecordingEventDispatcher[] disps = new RecordingEventDispatcher[4];
		disps[0] = new RecordingEventDispatcher();
		disps[1] = new RecordingEventDispatcher();
		disps[2] = new RecordingEventDispatcher();
		disps[3] = new RecordingEventDispatcher();
		ConcurrentEventDispatcher concDisp = new ConcurrentEventDispatcher(disps, ScaleStrategy.ROUND_ROBIN);

		int[] exp = { 0, 0, 0, 0 };
		for (int i = 0; i < 1000; i++) {
			concDisp.register(new RecordingEventHandler(i), 0);
			exp[i % 4]++;
			for (int x = 0; x < 4; x++) {
				assertEquals(disps[x].registerCalls, exp[x]);
			}
		}

		concDisp.close();
	}

	@Test
	public void scaleLeastLoad() throws IOException {
		RecordingEventDispatcher[] disps = new RecordingEventDispatcher[4];
		disps[0] = new RecordingEventDispatcher();
		disps[1] = new RecordingEventDispatcher();
		disps[2] = new RecordingEventDispatcher();
		disps[3] = new RecordingEventDispatcher();
		ConcurrentEventDispatcher concDisp = new ConcurrentEventDispatcher(disps, ScaleStrategy.LEAST_LOAD);

		Random rand = new Random();
		int[] exp = { 0, 0, 0, 0 };
		for (int i = 0; i < 1000; i++) {
			int which = rand.nextInt(4);
			for (int x = 0; x < 4; x++) {
				disps[x].loadAvg = (x == which) ? 1 : 2;
			}
			concDisp.register(new RecordingEventHandler(i), 0);
			exp[which]++;
			for (int x = 0; x < 4; x++) {
				assertEquals(disps[x].registerCalls, exp[x]);
			}
		}
		assertEquals(concDisp.getLoadAvg(), 1 + 2 + 2 + 2);

		concDisp.close();
	}

	@Test
	public void scaleLeastConnections() throws IOException {
		RecordingEventDispatcher[] disps = new RecordingEventDispatcher[4];
		disps[0] = new RecordingEventDispatcher();
		disps[1] = new RecordingEventDispatcher();
		disps[2] = new RecordingEventDispatcher();
		disps[3] = new RecordingEventDispatcher();
		ConcurrentEventDispatcher concDisp = new ConcurrentEventDispatcher(disps, ScaleStrategy.LEAST_CONNECTIONS);

		RecordingEventHandler[] handlers = new RecordingEventHandler[400];
		// register 400 -> 100 on each dispatcher
		for (int i = 0; i < 400; i++) {
			handlers[i] = new RecordingEventHandler(i);
			concDisp.register(handlers[i], 0);
		}

		for (int x = 0; x < 4; x++) {
			assertEquals(disps[x].registerCalls, 100);
		}
		assertEquals(concDisp.getNumEventHandlers(), 400);

		// unregister half from disps 3&4
		for (int i = 0; i < 200; i++) {
			if (i % 4 == 2 || i % 4 == 3) {
				concDisp.unregister(handlers[i]);
			}
		}

		assertEquals(disps[2].unregisterCalls, 50);
		assertEquals(disps[3].unregisterCalls, 50);
		assertEquals(concDisp.getNumEventHandlers(), 300);

		// register again
		for (int i = 0; i < 200; i++) {
			if (i % 4 == 2 || i % 4 == 3) {
				concDisp.register(handlers[i], 0);
			}
		}

		assertEquals(disps[0].unregisterCalls, 0);
		assertEquals(disps[1].unregisterCalls, 0);
		assertEquals(disps[2].unregisterCalls, 50);
		assertEquals(disps[3].unregisterCalls, 50);

		assertEquals(disps[0].registerCalls, 100);
		assertEquals(disps[1].registerCalls, 100);
		assertEquals(disps[2].registerCalls, 150);
		assertEquals(disps[3].registerCalls, 150);

		assertEquals(concDisp.getNumEventHandlers(), 400);

		concDisp.close();
	}

	public static class RecordingEventHandler implements EventHandler {

		public final long id;

		public int closeCalls;

		public int handleOpsCalls;

		public int handleTimeoutCalls;

		public RecordingEventHandler(long id) {
			this.id = id;
		}

		@Override
		public void close() throws IOException {
			closeCalls++;
		}

		@Override
		public SelectableChannel getChannel() {
			return null;
		}

		@Override
		public int handleOps(int ops) throws IOException {
			handleOpsCalls++;
			return 0;
		}

		@Override
		public boolean handleTimeout() throws IOException {
			handleTimeoutCalls++;
			return true;
		}

		@Override
		public long getEventHandlerId() {
			return id;
		}
	}

	public static class RecordingEventDispatcher implements EventDispatcher {

		public final Map<Long, EventHandler> map = new HashMap<Long, EventHandler>();

		public boolean open = true;

		public int closeCalls;

		public int registerCalls;

		public int unregisterCalls;

		public int setTimeoutCalls;

		public int unsetTimeoutCalls;

		public int getRegisteredOpsCalls;

		public int getNumEventHandlersCalls;

		public int loadAvg;

		@Override
		public void close() throws IOException {
			closeCalls++;
			assertTrue(open);
			open = false;
		}

		@Override
		public boolean isOpen() {
			return open;
		}

		@Override
		public void register(EventHandler eventHandler, int ops) {
			registerCalls++;
			assertNotNull(eventHandler);
			EventHandler oldHandler = map.put(eventHandler.getEventHandlerId(), eventHandler);
			assertNull(oldHandler);
		}

		@Override
		public void unregister(EventHandler eventHandler) {
			unregisterCalls++;
			assertNotNull(eventHandler);
			EventHandler handler = map.remove(eventHandler.getEventHandlerId());
			assertTrue(handler == eventHandler);
		}

		@Override
		public void setTimeout(EventHandler eventHandler, long timeout, TimeUnit timeUnit) {
			setTimeoutCalls++;
			assertNotNull(eventHandler);
			assertNotNull(timeUnit);
		}

		@Override
		public void unsetTimeout(EventHandler eventHandler) {
			unsetTimeoutCalls++;
			assertNotNull(eventHandler);
		}

		@Override
		public int getRegisteredOps(EventHandler eventHandler) {
			getRegisteredOpsCalls++;
			assertNotNull(eventHandler);
			return 0;
		}

		@Override
		public int getNumEventHandlers() {
			getNumEventHandlersCalls++;
			return map.size();
		}

		@Override
		public long getLoadAvg() {
			return loadAvg;
		}
	}
}
