package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.Extension;
import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.classloader.Part;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTestResources;
import net.sourceforge.jnlp.JNLPFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.net.URL;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFile;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createPartsFor;

public class DownloadServiceFunctionalityTest {

    @RepeatedTest(10)
    public void testPartDownloaded() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-2.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        new JnlpApplicationClassLoader(partsHandler);


        //than
        Assertions.assertFalse(partsHandler.isPartDownloaded("lazy-package"));
    }

    @RepeatedTest(10)
    public void testExtensionPartDownloaded() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-19.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

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
        final JNLPFile jnlpFile = createFile("integration-app-2.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertTrue(partsHandler.isPartDownloaded("lazy-package"));
    }

    @RepeatedTest(10)
    public void testDownloadPart() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-2.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        partsHandler.downloadPart("lazy-package");

        //than
        Assertions.assertTrue(partsHandler.isPartDownloaded("lazy-package"));
    }

    @RepeatedTest(10)
    public void testEagerPart() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-21.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertTrue(partsHandler.isPartDownloaded("eager-package"));
    }

    @RepeatedTest(10)
    public void testDownloadPartFromExtension() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-19.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);
        final URL extensionURL = IntegrationTestResources.load("integration-app-19-extension.jnlp");
        final Extension extension = new Extension(extensionURL, null);

        //when
        partsHandler.downloadPart("lazy-package", extension);

        //than
        Assertions.assertTrue(partsHandler.isPartDownloaded("lazy-package", extension));
        Assertions.assertFalse(partsHandler.isPartDownloaded("lazy-package"));
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
    }
}
