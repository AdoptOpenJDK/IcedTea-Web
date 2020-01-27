package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_3;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createDummyPartsHandlerFor;

public class OsSpecificClassloaderIntegrationTests {

    /**
     * A resource that has defined windows as os will be loaded on windows systems
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.WINDOWS)
    public void testWindowsOnlyResourceOnWindows() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-4.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined windows as os will be loaded on windows systems and classes can be loaded
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.WINDOWS)
    public void testWindowsOnlyResourceOnWindowsWithLoadClass() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-4.jnlp");

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
    }

    /**
     * A resource that has defined windows as os won't be loaded on other operation systems
     */
    @RepeatedTest(10)
    @DisabledOnOs(OS.WINDOWS)
    public void testWindowsOnlyResourceOnNotWindows() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-4.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(0, partsHandler.getDownloaded().size());
        Assertions.assertFalse(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined mac as os will be loaded on mac systems
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC)
    public void testMacOnlyResourceOnMac() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-5.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined mac as os will be loaded on mac systems and classes can be loaded
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC)
    public void testMacOnlyResourceOnMacWithLoadClass() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-5.jnlp");

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
    }

    /**
     * A resource that has defined mac as os won't be loaded on other operation systems
     */
    @RepeatedTest(10)
    @DisabledOnOs(OS.MAC)
    public void testMacOnlyResourceOnNotMac() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-5.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(0, partsHandler.getDownloaded().size());
        Assertions.assertFalse(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined linux as os will be loaded on linux systems
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.LINUX)
    public void testLinuxOnlyResourceOnLinux() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-6.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined linux as os will be loaded on linux systems and classes can be loaded
     */
    @RepeatedTest(10)
    @EnabledOnOs(OS.LINUX)
    public void testLinuxOnlyResourceOnLinuxWithLoadClass() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-6.jnlp");

        //when
        final ClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
    }

    /**
     * A resource that has defined linux as os won't be loaded on other operation systems
     */
    @RepeatedTest(10)
    @DisabledOnOs(OS.LINUX)
    public void testLinuxOnlyResourceOnNotLinux() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-6.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(0, partsHandler.getDownloaded().size());
        Assertions.assertFalse(partsHandler.hasTriedToDownload(JAR_1));
    }

    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC)
    public void testMacOnlyResourceInJreOnMac() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-24.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_3));
    }

    @RepeatedTest(10)
    @EnabledOnOs(OS.WINDOWS)
    public void testWindowsOnlyResourceInJreOnWindows() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-24.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
    }

    @RepeatedTest(10)
    @EnabledOnOs(OS.LINUX)
    public void testLinuxOnlyResourceInJreOnLinux() throws Exception {
        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("integration-app-24.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_2));
    }
}
