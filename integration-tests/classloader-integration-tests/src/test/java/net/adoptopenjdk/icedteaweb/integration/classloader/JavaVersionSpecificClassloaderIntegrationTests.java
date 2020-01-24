package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.classloader.Part;
import net.adoptopenjdk.icedteaweb.classloader.PartsHandler;
import net.sourceforge.jnlp.JNLPFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFile;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createPartsFor;

public class JavaVersionSpecificClassloaderIntegrationTests {

    /**
     * Resources that are defined as part of a not matching Java version won't be loaded
     */
    @RepeatedTest(10)
    public void testNotLoadJarFromNotMatchingJavaVersion() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-9.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
        Assertions.assertFalse(partsHandler.hasTriedToDownload(JAR_2));
    }

    /**
     * Resources that are defined as part of a not matching Java version won't be loaded
     */
    @RepeatedTest(10)
    public void testNotLoadJarFromNotMatchingJavaVersion2() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-14.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
        Assertions.assertFalse(partsHandler.hasTriedToDownload(JAR_2));
    }

    /**
     * Resources that are defined as part of a matching Java version will be loaded
     */
    @RepeatedTest(10)
    public void testLoadJarFromMatchingJavaVersion() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-10.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }
}
