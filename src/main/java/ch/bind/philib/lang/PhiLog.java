package ch.bind.philib.lang;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class PhiLog {

	private final Logger logger;

	public PhiLog(Class<?> clazz) {
		logger = Logger.getLogger(clazz.getName());
	}

	public void info(String msg) {
		logger.log(Level.INFO, msg);
	}

	public void info(String msg, Throwable cause) {
		logger.log(Level.INFO, msg, cause);
	}

	public void warn(String msg) {
		logger.log(Level.WARNING, msg);
	}

	public void warn(String msg, Throwable cause) {
		logger.log(Level.WARNING, msg, cause);
	}
}
