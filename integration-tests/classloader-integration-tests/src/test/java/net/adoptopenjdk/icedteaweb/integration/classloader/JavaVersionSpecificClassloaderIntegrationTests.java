package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.sourceforge.jnlp.runtime.classloader2.JnlpApplicationClassLoader;
import net.sourceforge.jnlp.runtime.classloader2.Part;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFor;

public class JavaVersionSpecificClassloaderIntegrationTests {

    @Test
    public void testNotLoadJarFromNotMatchingJavaVersion() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-9.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertFalse(jarProvider.hasTriedToDownload(JAR_2));
    }

    @Test
    public void testNotLoadJarFromNotMatchingJavaVersion2() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-14.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertFalse(jarProvider.hasTriedToDownload(JAR_2));
    }

    @Test
    public void testLoadJarFromMatchingJavaVersion() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-10.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(2, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_2));
    }
}
