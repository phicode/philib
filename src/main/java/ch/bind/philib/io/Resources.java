package ch.bind.philib.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import ch.bind.philib.conf.Config;

public abstract class Resources {

	protected Resources() {
	}

	/**
	 * Opens different resources based on the scheme being used.
	 * <p>
	 * <h2>Classpath</h2>
	 * <p></p>
	 * Resources starting with <b>classpath:</b> have this prefix removed and all
	 * <ul>
	 * <li>classpath:</li>
	 * </ul>
	 *
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public static InputStream openResource(String resource) throws IOException {
		if (resource.startsWith("classpath:")) {
			String cp = resource.substring("classpath:".length());
			InputStream is = Resources.class.getResourceAsStream(cp);
			if (is == null) {
				throw new FileNotFoundException("classpath resource not found: " + resource);
			}
			return is;
		}
		return new FileInputStream(resource);
	}

	public static Map<String, String> loadPropertiesFile(String resource) throws IOException {
		InputStream is = openResource(resource);
		try {
			Properties props = new Properties();
			props.load(is);
			return Config.toMap(props);
		} finally {
			SafeCloseUtil.close(is);
		}
	}
}
