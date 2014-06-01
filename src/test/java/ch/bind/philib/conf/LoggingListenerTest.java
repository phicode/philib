package ch.bind.philib.conf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class LoggingListenerTest {

	@Test
	public void defaultConstructor() {
		LoggingListener ll = new LoggingListener();
		assertNotNull(ll.getLogger());
		assertSame(ll.getLogger(), LoggerFactory.getLogger(LoggingListener.class));
	}

	@Test
	public void added() {
		Logger log = mock(Logger.class);
		LoggingListener ll = new LoggingListener(log);
		ll.added("key", "value");
		verify(log).info("added '%s': '%s'", "key", "value");
		verifyNoMoreInteractions(log);
	}

	@Test
	public void changed() {
		Logger log = mock(Logger.class);
		LoggingListener ll = new LoggingListener(log);
		ll.changed("key", "oldValue", "newValue");
		verify(log).info("changed '%s': '%s' -> '%s'", "key", "oldValue", "newValue");
		verifyNoMoreInteractions(log);
	}

	@Test
	public void removed() {
		Logger log = mock(Logger.class);
		LoggingListener ll = new LoggingListener(log);
		ll.removed("key", "oldValue");
		verify(log).info("removed '%s': '%s'", "key", "oldValue");
		verifyNoMoreInteractions(log);
	}
}
