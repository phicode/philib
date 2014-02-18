package ch.bind.philib.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingListener implements ConfigListener {

	private final Logger log;

	public LoggingListener() {
		this(LoggerFactory.getLogger(LoggingListener.class));
	}

	public LoggingListener(Logger log) {
		this.log = log;
	}

	@Override
	public void changed(String key, String oldValue, String newValue) {
		log.info("changed '%s': '%s' -> '%s'", key, oldValue, newValue);
	}

	@Override
	public void removed(String key, String oldValue) {
		log.info("removed '%s': '%s'", key, oldValue);
	}

	@Override
	public void added(String key, String value) {
		log.info("added '%s': '%s'", key, value);
	}
}
