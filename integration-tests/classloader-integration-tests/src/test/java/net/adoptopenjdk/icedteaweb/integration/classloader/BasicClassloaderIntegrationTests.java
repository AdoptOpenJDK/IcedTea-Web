package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.sourceforge.jnlp.runtime.classloader2.JarExtractor;
import net.sourceforge.jnlp.runtime.classloader2.JnlpApplicationClassLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_B;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFor;

public class BasicClassloaderIntegrationTests {

    @Test
    public void testLoadClassFromEagerJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-1.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);

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
        final JarExtractor jarExtractor = createFor("integration-app-2.jnlp");

        //when
        new JnlpApplicationClassLoader(jarExtractor, jarProvider);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
    }

    @Test
    public void testLoadClassFromLazyJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-2.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    @Test
    public void testLoadClassFromLazyJarWithRecursive() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-7.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    @Test
    public void testLoadClassFromLazyJarWithoutRecursive() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-8.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);

        //when
        Assertions.assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass(CLASS_A));
    }

    @Test
    public void testLazyJarOnlyDownloadedOnce() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-2.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);

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

    @Test
    public void testFullPartDownloaded() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-3.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(2, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_2));
    }

    @Test
    public void testMultipleResources() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-11.jnlp");
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);

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

}
