package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.sourceforge.jnlp.runtime.classloader2.JnlpApplicationClassLoader;
import net.sourceforge.jnlp.runtime.classloader2.Part;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_B;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFor;

public class BasicClassloaderIntegrationTests {

    /**
     * When loading a JNLP file the eager jars should be directly downloaded and accessible by the classloader
     */
    @Test
    public void testEagerJarLoadedAtStart() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-1.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * When loading a JNLP file classes from eager jar can be loaded
     */
    @Test
    public void testLoadClassFromEagerJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-1.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * When loading a JNLP file a lazy jar should not be directly downloaded
     */
    @Test
    public void testClassFromLazyJarNotInitialLoaded() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-2.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
    }

    /**
     * When accessing a class from a lazy jar the classloader will trigger the download of the jar and load the class
     */
    @Test
    public void testLoadClassFromLazyJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-2.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * When accessing a class from a lazy jar the classloader will trigger the download of the jar and load the class
     * Here the recursive attribute is checked
     */
    @Test
    public void testLoadClassFromLazyJarWithRecursive() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-7.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * if recursive attribute is not defined only direct classes in the package of a part can be downloaded
     */
    @Test
    public void testLoadClassFromLazyJarWithoutRecursive() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-8.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        Assertions.assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass(CLASS_A));
    }

    /**
     * When accessing a class from a lazy jar multiple times the jar is only downloaded one time
     */
    @Test
    public void testLazyJarOnlyDownloadedOnce() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-2.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass1 = classLoader.loadClass(CLASS_A);
        final Class<?> loadedClass2 = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass1);
        Assertions.assertEquals(classLoader, loadedClass1.getClassLoader());
        Assertions.assertNotNull(loadedClass2);
        Assertions.assertEquals(classLoader, loadedClass2.getClassLoader());
        Assertions.assertTrue(loadedClass1 == loadedClass2);
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * When accessing a class from a lazy jar all jars that are in the same part will be downloaded
     */
    @Test
    public void testFullPartDownloaded() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-3.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(2, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_2));
    }

    /**
     * When a JNLP contains multiple resource tags all jars of the resources will be downloaded correctly
     */
    @Test
    public void testMultipleResources() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-11.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass1 = classLoader.loadClass(CLASS_A);
        final Class<?> loadedClass2 = classLoader.loadClass(CLASS_B);

        //than
        Assertions.assertNotNull(loadedClass1);
        Assertions.assertEquals(classLoader, loadedClass1.getClassLoader());
        Assertions.assertNotNull(loadedClass2);
        Assertions.assertEquals(classLoader, loadedClass2.getClassLoader());
        Assertions.assertEquals(2, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_2));
    }

    /**
     * When a part has lazy and eager parts it will be automatically downloaded
     */
    @Test
    public void testEagerPart() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-21.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(2, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_2));
    }

    /**
     * If more than one lazy part matches for the needed class all parts should be downloaded
     */
    @Test
    public void testAllLazyPartsLoaded() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-23.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_B);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(2, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_2));
    }

}
