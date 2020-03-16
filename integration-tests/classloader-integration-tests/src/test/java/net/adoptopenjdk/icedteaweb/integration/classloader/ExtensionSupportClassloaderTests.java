package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.classloader.PartsHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_B;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createDummyPartsHandlerFor;

public class ExtensionSupportClassloaderTests {

    /**
     * A part of an extension JNLP will not be automatically downloaded if all jars of the part are lazy
     */
    @RepeatedTest(10)
    public void testClassFromLazyJarNotInitialLoaded() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-19.jnlp");

        //when
        createAndInitClassloader(partsHandler);

        //than
        Assertions.assertEquals(0, partsHandler.getDownloaded().size());
    }

    /**
     * A lazy part of an extension JNLP will be downloaded if a class of the part is loaded
     */
    @RepeatedTest(10)
    public void testLoadClassFromLazyJar() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-19.jnlp");

        //when
        final ClassLoader classLoader = createAndInitClassloader(partsHandler);
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
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-20.jnlp");

        //when
        createAndInitClassloader(partsHandler);

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
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-20.jnlp");

        //when
        final ClassLoader classLoader = createAndInitClassloader(partsHandler);
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
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-22.jnlp");

        //when
        final ClassLoader classLoader = createAndInitClassloader(partsHandler);
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
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-22.jnlp");

        //when
        final ClassLoader classLoader = createAndInitClassloader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_B);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_2));
    }


    private ClassLoader createAndInitClassloader(PartsHandler partsHandler) {
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        classLoader.initializeEagerJars();
        return classLoader;
    }
}
