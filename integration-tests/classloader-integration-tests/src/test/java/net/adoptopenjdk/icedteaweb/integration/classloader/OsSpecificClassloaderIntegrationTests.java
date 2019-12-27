package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.sourceforge.jnlp.runtime.classloader2.JnlpApplicationClassLoader;
import net.sourceforge.jnlp.runtime.classloader2.Part;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.List;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.CLASS_A;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFor;

public class OsSpecificClassloaderIntegrationTests {

    /**
     * A resource that has defined windows as os will be loaded on windows systems
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.WINDOWS)
    public void testWindowsOnlyResourceOnWindows() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-4.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined windows as os will be loaded on windows systems and classes can be loaded
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.WINDOWS)
    public void testWindowsOnlyResourceOnWindowsWithLoadClass() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-4.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
    }

    /**
     * A resource that has defined windows as os won't be loaded on other operation systems
     */
    @Test
    @RepeatedTest(10)
    @DisabledOnOs(OS.WINDOWS)
    public void testWindowsOnlyResourceOnNotWindows() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-4.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
        Assertions.assertFalse(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined mac as os will be loaded on mac systems
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC)
    public void testMacOnlyResourceOnMac() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-5.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined mac as os will be loaded on mac systems and classes can be loaded
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.MAC)
    public void testMacOnlyResourceOnMacWithLoadClass() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-5.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
    }

    /**
     * A resource that has defined mac as os won't be loaded on other operation systems
     */
    @Test
    @RepeatedTest(10)
    @DisabledOnOs(OS.MAC)
    public void testMacOnlyResourceOnNotMac() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-5.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
        Assertions.assertFalse(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined linux as os will be loaded on linux systems
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.LINUX)
    public void testLinuxOnlyResourceOnLinux() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-6.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
    }

    /**
     * A resource that has defined linux as os will be loaded on linux systems and classes can be loaded
     */
    @Test
    @RepeatedTest(10)
    @EnabledOnOs(OS.LINUX)
    public void testLinuxOnlyResourceOnLinuxWithLoadClass() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-6.jnlp").getParts();
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(parts, jarProvider);

        //when
        final Class<?> loadedClass = classLoader.loadClass(CLASS_A);

        //than
        Assertions.assertNotNull(loadedClass);
        Assertions.assertEquals(classLoader, loadedClass.getClassLoader());
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
    }

    /**
     * A resource that has defined linux as os won't be loaded on other operation systems
     */
    @Test
    @RepeatedTest(10)
    @DisabledOnOs(OS.LINUX)
    public void testLinuxOnlyResourceOnNotLinux() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-6.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(0, jarProvider.getDownloaded().size());
        Assertions.assertFalse(jarProvider.hasTriedToDownload(JAR_1));
    }
}
