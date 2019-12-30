package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.runtime.classloader2.JnlpApplicationClassLoader;
import net.sourceforge.jnlp.runtime.classloader2.Part;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.List;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_WITH_NATIVE;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createPartsFor;

public class NativeSupportClassloaderIntegrationTests {

    private static final String NATIVE_CLASS = "net.adoptopenjdk.integration.ClassWithNativeCall";

    /**
     * A jar that is defined by nativelib tag will be downloaded
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void loadJarWithNativeContent() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-15.jnlp");

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_WITH_NATIVE));
    }

    /**
     * A class in a jar that is defined by nativelib tag can be loaded
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void loadClassWithNativeMethod() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-15.jnlp");
        final ClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadClass = classLoader.loadClass(NATIVE_CLASS);
        
        //than
        Assertions.assertNotNull(loadClass);
    }

    /**
     * A native method that native lib is part of a jar can be called
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void callNativeMethod() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-15.jnlp");
        final ClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);
        final Class<?> loadClass = classLoader.loadClass(NATIVE_CLASS);
        
        //when
        final Object classInstance = loadClass.newInstance();
        final Object result = loadClass.getMethod("callNative").invoke(classInstance);

        //than
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Hello from native world!", result);
    }

    /**
     * If the JNLP does not have a security environment but has nativelib parts the initialization will crash
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void doNotLoadNativeWithoutSecurityEnvironment() throws Exception {
        Assertions.assertThrows(ParseException.class, () -> createPartsFor("integration-app-16.jnlp"));
    }

    /**
     * If a jar is defined as jar (and not as nativelib) in the JNLP than native libraries that are part of the jar can not be loaded
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void doNotLoadNativeForSimpleJarDesc() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-17.jnlp");
        final ClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertThrows(UnsatisfiedLinkError.class, () -> classLoader.loadClass(NATIVE_CLASS).newInstance());
    }

    /**
     * if a nativelib is defined as lazy than the content (with the native content) won't be downloaded automatically
     * @throws Exception
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void doNotLoadLazyNativeLibAtStart() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-18.jnlp");

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
    }

    /**
     * if a nativelib is defined as lazy than the content (with the native content) will be loaded once a class from the
     * lib will be loaded
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void callNativeMethodFromLazyJar() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createPartsFor("integration-app-18.jnlp");
        final ClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);
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
