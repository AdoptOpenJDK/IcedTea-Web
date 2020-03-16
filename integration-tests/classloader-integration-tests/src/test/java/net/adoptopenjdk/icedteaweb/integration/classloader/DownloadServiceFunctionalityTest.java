package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.Extension;
import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTestResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.net.URL;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createDummyPartsHandlerFor;

public class DownloadServiceFunctionalityTest {

    @RepeatedTest(10)
    public void testPartDownloaded() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-2.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);


        //than
        Assertions.assertFalse(partsHandler.isPartDownloaded("lazy-package"));
    }

    @RepeatedTest(10)
    public void testExtensionPartDownloaded() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-19.jnlp");

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        classLoader.loadClass(CLASS_A);

        //than
        final URL extensionURL = IntegrationTestResources.load("integration-app-19-extension.jnlp");
        final Extension extension = new Extension(extensionURL, null);
        Assertions.assertTrue(partsHandler.isPartDownloaded("lazy-package", extension));
    }

    @RepeatedTest(10)
    public void testPartDownloaded2() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-2.jnlp");

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertTrue(partsHandler.isPartDownloaded("lazy-package"));
    }

    @RepeatedTest(10)
    public void testDownloadPart() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-2.jnlp");

        //when
        partsHandler.downloadPart("lazy-package");

        //than
        Assertions.assertTrue(partsHandler.isPartDownloaded("lazy-package"));
    }

    @RepeatedTest(10)
    public void testEagerPart() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-21.jnlp");

        //when
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        classLoader.initializeEagerJars();


        //than
        Assertions.assertTrue(partsHandler.isPartDownloaded("eager-package"));
    }

    @RepeatedTest(10)
    public void testDownloadPartFromExtension() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-19.jnlp");
        final URL extensionURL = IntegrationTestResources.load("integration-app-19-extension.jnlp");
        final Extension extension = new Extension(extensionURL, null);

        //when
        partsHandler.downloadPart("lazy-package", extension);

        //than
        Assertions.assertTrue(partsHandler.isPartDownloaded("lazy-package", extension));
        Assertions.assertFalse(partsHandler.isPartDownloaded("lazy-package"));
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));

    }
}
