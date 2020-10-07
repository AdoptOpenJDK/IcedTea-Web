package net.sourceforge.jnlp.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.jnlp.util.FileTestUtils;
import net.sourceforge.jnlp.util.FileUtils;

public class CachedJarFileCallbackTest {
	private File tempDirectory;

	@Before
	public void before() throws IOException {
		tempDirectory = FileTestUtils.createTempDirectory();
	}

	@After
	public void after() throws IOException {
		FileUtils.recursiveDelete(tempDirectory, tempDirectory.getParentFile());
	}

	@Test
	public void testRetrieve() throws Exception {
		List<String> names = Arrays.asList("test1.0.jar", "test@1.0.jar");
		
		for (String name: names) {
			// URL-encode the filename
			name = URLEncoder.encode(name, StandardCharsets.UTF_8.name());
			// create temp jar file
			File jarFile = new File(tempDirectory, name);
			FileTestUtils.createJarWithContents(jarFile /* no contents */);

			// JNLPClassLoader.activateJars uses toUri().toURL() to get the local file URL
			URL localUrl = jarFile.toURI().toURL();
			URL remoteUrl = new URL("http://localhost/" + name);
			// add jar to cache
			CachedJarFileCallback cachedJarFileCallback = CachedJarFileCallback.getInstance();
			cachedJarFileCallback.addMapping(remoteUrl, localUrl);
			// retrieve from cache (throws exception if file not found)
			try (JarFile fromCacheJarFile = cachedJarFileCallback.retrieve(remoteUrl)) {
				// nothing to do, we just wanted to make sure that the local file existed
			}
		}
	}
}
