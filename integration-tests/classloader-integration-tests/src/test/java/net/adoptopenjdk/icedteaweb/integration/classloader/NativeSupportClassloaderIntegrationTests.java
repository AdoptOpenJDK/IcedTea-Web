package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_WITH_NATIVE;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createDummyPartsHandlerFor;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFile;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createPartsFor;

public class NativeSupportClassloaderIntegrationTests {

    private static final String NATIVE_CLASS = "net.adoptopenjdk.integration.ClassWithNativeCall";

    /**
     * A jar that is defined by nativelib tag will be downloaded
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void loadJarWithNativeContent() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-15.jnlp");

        //when
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        classLoader.initializeEagerJars();

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_WITH_NATIVE));
    }

    /**
     * A class in a jar that is defined by nativelib tag can be loaded
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void loadClassWithNativeMethod() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-15.jnlp");

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadClass = classLoader.loadClass(NATIVE_CLASS);
        
        //than
        Assertions.assertNotNull(loadClass);
    }

    /**
     * A native method that native lib is part of a jar can be called
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void callNativeMethod() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-15.jnlp");

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadClass = classLoader.loadClass(NATIVE_CLASS);
        final Object classInstance = loadClass.newInstance();
        final Object result = loadClass.getMethod("callNative").invoke(classInstance);

        //than
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Hello from native world!", result);
    }

    /**
     * If the JNLP does not have a security environment but has nativelib parts the initialization will crash
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void doNotLoadNativeWithoutSecurityEnvironment() {
        Assertions.assertThrows(ParseException.class, () -> createPartsFor(createFile("integration-app-16.jnlp")));
    }

    /**
     * If a jar is defined as jar (and not as nativelib) in the JNLP than native libraries that are part of the jar can not be loaded
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void doNotLoadNativeForSimpleJarDesc() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-17.jnlp");

        //than
        Assertions.assertThrows(UnsatisfiedLinkError.class, () -> {
            final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
            classLoader.loadClass(NATIVE_CLASS).newInstance();
        });
    }

    /**
     * if a nativelib is defined as lazy than the content (with the native content) won't be downloaded automatically
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void doNotLoadLazyNativeLibAtStart() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-18.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(0, partsHandler.getDownloaded().size());
    }

    /**
     * if a nativelib is defined as lazy than the content (with the native content) will be loaded once a class from the
     * lib will be loaded
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC) // We only have native lib for MAC so far...
    public void callNativeMethodFromLazyJar() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-18.jnlp");

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadClass = classLoader.loadClass(NATIVE_CLASS);
        final Object classInstance = loadClass.newInstance();
        final Object result = loadClass.getMethod("callNative").invoke(classInstance);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_WITH_NATIVE));
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Hello from native world!", result);
    }
}
