package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.runtime.classloader2.JarExtractor;
import net.sourceforge.jnlp.runtime.classloader2.JnlpApplicationClassLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_WITH_NATIVE;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFor;

public class NativeSupportClassloaderIntegrationTests {

    private static final String NATIVE_CLASS = "net.adoptopenjdk.integration.ClassWithNativeCall";

    @Test
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void loadJarWithNativeContent() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-15.jnlp");

        //when
        new JnlpApplicationClassLoader(jarExtractor, jarProvider);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_WITH_NATIVE));
    }

    @Test
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void loadClassWithNativeMethod() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-15.jnlp");
        final ClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);

        //when
        final Class<?> loadClass = classLoader.loadClass(NATIVE_CLASS);
        
        //than
        Assertions.assertNotNull(loadClass);
    }

    @Test
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void callNativeMethod() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-15.jnlp");
        final ClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);
        final Class<?> loadClass = classLoader.loadClass(NATIVE_CLASS);
        
        //when
        final Object classInstance = loadClass.newInstance();
        final Object result = loadClass.getMethod("callNative").invoke(classInstance);

        //than
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Hello from native world!", result);
    }

    @Test
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void doNotLoadNativeWithoutSecurityEnvironment() throws Exception {
        Assertions.assertThrows(ParseException.class, () -> createFor("integration-app-16.jnlp"));
    }

    @Test
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void doNotLoadNativeForSimpleJarDesc() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-17.jnlp");
        final ClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);

        //than
        Assertions.assertThrows(UnsatisfiedLinkError.class, () -> classLoader.loadClass(NATIVE_CLASS));
    }

    @Test
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void doNotLoadLazyNativeLibAtStart() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-18.jnlp");

        //when
        new JnlpApplicationClassLoader(jarExtractor, jarProvider);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
    }

    @Test
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void callNativeMethodFromLazyJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JarExtractor jarExtractor = createFor("integration-app-18.jnlp");
        final ClassLoader classLoader = new JnlpApplicationClassLoader(jarExtractor, jarProvider);
        final Class<?> loadClass = classLoader.loadClass(NATIVE_CLASS);

        //when
        final Object classInstance = loadClass.newInstance();
        final Object result = loadClass.getMethod("callNative").invoke(classInstance);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_WITH_NATIVE));
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Hello from native world!", result);
    }
}
