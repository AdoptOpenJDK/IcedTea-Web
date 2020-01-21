package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.classloader.Part;
import net.adoptopenjdk.icedteaweb.classloader.PartsHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_B;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createPartsFor;

public class ExtensionSupportClassloaderTests {

    /**
     * A part of an extension JNLP will not be automatically downloaded if all jars of the part are lazy
     */
    @RepeatedTest(10)
    public void testClassFromLazyJarNotInitialLoaded() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-19.jnlp");
        final PartsHandler partsHandler = new PartsHandler(parts, jarProvider);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
    }

    /**
     * A lazy part of an extension JNLP will be downloaded if a class of the part is loaded
     */
    @RepeatedTest(10)
    public void testLoadClassFromLazyJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-19.jnlp");
        final PartsHandler partsHandler = new PartsHandler(parts, jarProvider);

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * An eager jar of an extension JNLP will automatically be downloaded
     */
    @RepeatedTest(10)
    public void testLoadClassFromEagerJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-20.jnlp");
        final PartsHandler partsHandler = new PartsHandler(parts, jarProvider);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * A class from an eager jar of an extension JNLP can be loaded
     */
    @RepeatedTest(10)
    public void testLoadClassFromEagerJar2() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-20.jnlp");
        final PartsHandler partsHandler = new PartsHandler(parts, jarProvider);

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * Parts with the same name in the main JNLP and an extension JNLP do not belong together
     */
    @RepeatedTest(10)
    public void testPartIsJnlpExclusive() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-22.jnlp");
        final PartsHandler partsHandler = new PartsHandler(parts, jarProvider);

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * Parts with the same name in the main JNLP and an extension JNLP do not belong together
     */
    @RepeatedTest(10)
    public void testPartIsJnlpExclusive2() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-22.jnlp");
        final PartsHandler partsHandler = new PartsHandler(parts, jarProvider);

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_B);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_2));
    }

}
