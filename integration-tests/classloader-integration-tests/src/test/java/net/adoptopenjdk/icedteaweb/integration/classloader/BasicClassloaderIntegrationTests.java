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

public class BasicClassloaderIntegrationTests {

    /**
     * When loading a JNLP file the eager jars should be directly downloaded and accessible by the classloader
     */
    @RepeatedTest(10)
    public void testEagerJarLoadedAtStart() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-1.jnlp");

        //when
        createAndInitClassloader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * When loading a JNLP file classes from eager jar can be loaded
     */
    @RepeatedTest(10)
    public void testLoadClassFromEagerJar() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-1.jnlp");

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
     * When loading a JNLP file a lazy jar should not be directly downloaded
     */
    @RepeatedTest(10)
    public void testClassFromLazyJarNotInitialLoaded() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-2.jnlp");

        //when
        createAndInitClassloader(partsHandler);

        //than
        Assertions.assertEquals(0, partsHandler.getDownloaded().size());
    }

    /**
     * When accessing a class from a lazy jar the classloader will trigger the download of the jar and load the class
     */
    @RepeatedTest(10)
    public void testLoadClassFromLazyJar() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-2.jnlp");
        ;

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
     * When accessing a class from a lazy jar the classloader will trigger the download of the jar and load the class
     * Here the recursive attribute is checked
     */
    @RepeatedTest(10)
    public void testLoadClassFromLazyJarWithRecursive() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-7.jnlp");

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
     * if recursive attribute is not defined only direct classes in the package of a part can be downloaded.
     */
    @RepeatedTest(10)
    public void testLoadClassFromLazyJarWithoutRecursive() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-8.jnlp");

        //when
        final ClassLoader classLoader = createAndInitClassloader(partsHandler);
        try {
            classLoader.loadClass(CLASS_A);
            Assertions.fail("should not have found the class");
        } catch (ClassNotFoundException ignored) {
        }

        //than
        Assertions.assertEquals(0, partsHandler.getDownloaded().size());
        Assertions.assertFalse(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * When accessing a class from a lazy jar multiple times the jar is only downloaded one time
     */
    @RepeatedTest(10)
    public void testLazyJarOnlyDownloadedOnce() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-2.jnlp");

        //when
        final ClassLoader classLoader = createAndInitClassloader(partsHandler);
        final Class<?> loadedClass1 = classLoader.loadClass(CLASS_A);
        final Class<?> loadedClass2 = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass1);
        Assertions.assertEquals(classLoader, loadedClass1.getClassLoader());
        Assertions.assertNotNull(loadedClass2);
        Assertions.assertEquals(classLoader, loadedClass2.getClassLoader());
        Assertions.assertSame(loadedClass1, loadedClass2);
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * When accessing a class from a lazy jar all jars that are in the same part will be downloaded
     */
    @RepeatedTest(10)
    public void testFullPartDownloaded() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-3.jnlp");

        //when
        final ClassLoader classLoader = createAndInitClassloader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(2, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_2));
    }

    /**
     * When a JNLP contains multiple resource tags all jars of the resources will be downloaded correctly
     */
    @RepeatedTest(10)
    public void testMultipleResources() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-11.jnlp");

        //when
        final ClassLoader classLoader = createAndInitClassloader(partsHandler);
        final Class<?> loadedClass1 = classLoader.loadClass(CLASS_A);
        final Class<?> loadedClass2 = classLoader.loadClass(CLASS_B);

        //than
        Assertions.assertNotNull(loadedClass1);
        Assertions.assertEquals(classLoader, loadedClass1.getClassLoader());
        Assertions.assertNotNull(loadedClass2);
        Assertions.assertEquals(classLoader, loadedClass2.getClassLoader());
        Assertions.assertEquals(2, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_2));
    }

    /**
     * When a part has lazy and eager parts it will be automatically downloaded
     */
    @RepeatedTest(10)
    public void testEagerPart() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-21.jnlp");

        //when
        createAndInitClassloader(partsHandler);

        //than
        Assertions.assertEquals(2, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_2));
    }

    /**
     * If more than one lazy part matches for the needed class all parts should be downloaded
     */
    @RepeatedTest(10)
    public void testAllLazyPartsLoaded() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-23.jnlp");

        //when
        final ClassLoader classLoader = createAndInitClassloader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_B);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(2, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_2));
    }

    private ClassLoader createAndInitClassloader(PartsHandler partsHandler) {
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        classLoader.initializeEagerJars();
        return classLoader;
    }
}
