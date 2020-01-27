package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createDummyPartsHandlerFor;

public class JavaVersionSpecificClassloaderIntegrationTests {

    /**
     * Resources that are defined as part of a not matching Java version won't be loaded
     */
    @RepeatedTest(10)
    public void testNotLoadJarFromNotMatchingJavaVersion() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-9.jnlp");

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
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-14.jnlp");

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
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-10.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }
}
