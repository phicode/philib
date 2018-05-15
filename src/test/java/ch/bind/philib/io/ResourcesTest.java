package ch.bind.philib.io;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.testng.annotations.Test;

public class ResourcesTest {

	@Test(expectedExceptions = FileNotFoundException.class)
	public void openResourceClasspathNotFound() throws IOException {
		Resources.openResource("classpath:/does-not-exist");
	}

	@Test
	public void openResourceClasspath() throws IOException {
		try (InputStream is = Resources.openResource("classpath:/words_en")) {
			assertNotNull(is);
		}
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void openResourceFileNotFound() throws IOException {
		Resources.openResource("/does-not-exist");
	}

	@Test
	public void openResourceFile() throws IOException {
		try (InputStream is = Resources.openResource("./src/test/resources/words_en")) {
			assertNotNull(is);
			assertEquals(is.getClass(), FileInputStream.class);
		}
	}
}