package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.classloader.Part;
import net.sourceforge.jnlp.JNLPFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_B;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFile;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createPartsFor;

public class ExtensionSupportClassloaderTests {

    /**
     * A part of an extension JNLP will not be automatically downloaded if all jars of the part are lazy
     */
    @RepeatedTest(10)
    public void testClassFromLazyJarNotInitialLoaded() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-19.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(0, partsHandler.getDownloaded().size());
    }

    /**
     * A lazy part of an extension JNLP will be downloaded if a class of the part is loaded
     */
    @RepeatedTest(10)
    public void testLoadClassFromLazyJar() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-19.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * An eager jar of an extension JNLP will automatically be downloaded
     */
    @RepeatedTest(10)
    public void testLoadClassFromEagerJar() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-20.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * A class from an eager jar of an extension JNLP can be loaded
     */
    @RepeatedTest(10)
    public void testLoadClassFromEagerJar2() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-20.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * Parts with the same name in the main JNLP and an extension JNLP do not belong together
     */
    @RepeatedTest(10)
    public void testPartIsJnlpExclusive() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-22.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * Parts with the same name in the main JNLP and an extension JNLP do not belong together
     */
    @RepeatedTest(10)
    public void testPartIsJnlpExclusive2() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-22.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_B);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_2));
    }

}
