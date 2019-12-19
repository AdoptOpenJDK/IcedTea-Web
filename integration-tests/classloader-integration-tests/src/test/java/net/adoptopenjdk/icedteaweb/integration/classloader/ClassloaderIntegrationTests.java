package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.classloader2.JnlpApplicationClassLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class ClassloaderIntegrationTests {

    @Test
    public void testLoadClassFromEagerJar() throws Exception {
        //given
        final JNLPFile file = new JNLPFile(ClassloaderIntegrationTests.class.getResource("integration-app-1.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, new DummyJarProvider());

        //when
        final Class<?> loadedClass = classLoader.loadClass("net.adoptopenjdk.integration.ClassA");

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
    }

    @Test
    public void testClassFromLazyJarNotInitialLoaded() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = new JNLPFile(ClassloaderIntegrationTests.class.getResource("integration-app-2.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
    }

    @Test
    public void testLoadClassFromLazyJar() throws Exception {
        //given
        final JNLPFile file = new JNLPFile(ClassloaderIntegrationTests.class.getResource("integration-app-2.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, new DummyJarProvider());

        //when
        final Class<?> loadedClass = classLoader.loadClass("net.adoptopenjdk.integration.ClassA");

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
    }

    @Test
    public void testFullPartDownloaded() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = new JNLPFile(ClassloaderIntegrationTests.class.getResource("integration-app-3.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass("net.adoptopenjdk.integration.ClassA");

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(2, jarProvider.getDownloaded().size());
    }

    private class DummyJarProvider implements Function<JARDesc, URL> {

        private final List<JARDesc> downloaded = new CopyOnWriteArrayList<>();

        @Override
        public URL apply(final JARDesc jarDesc) {
            System.out.println("Should load " + jarDesc.getLocation());
            downloaded.add(jarDesc);
            return jarDesc.getLocation();
        }

        public boolean hasTriedToDownload(final String name) {
            return downloaded.stream()
                    .anyMatch(jar -> jar.getLocation().toString().endsWith(name));
        }

        public List<JARDesc> getDownloaded() {
            return Collections.unmodifiableList(downloaded);
        }
    }

}
