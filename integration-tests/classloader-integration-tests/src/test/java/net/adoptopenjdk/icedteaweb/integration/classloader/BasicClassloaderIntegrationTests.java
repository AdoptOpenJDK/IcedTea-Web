package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.classloader2.JnlpApplicationClassLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFile;

public class BasicClassloaderIntegrationTests {

    @Test
    public void testLoadClassFromEagerJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = createFile("integration-app-1.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    @Test
    public void testClassFromLazyJarNotInitialLoaded() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = createFile("integration-app-2.jnlp");

        //when
        new JnlpApplicationClassLoader(file, jarProvider);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
    }

    @Test
    public void testLoadClassFromLazyJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = createFile("integration-app-2.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    @Test
    public void testFullPartDownloaded() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = createFile("integration-app-3.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(2, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_2));
    }

}
