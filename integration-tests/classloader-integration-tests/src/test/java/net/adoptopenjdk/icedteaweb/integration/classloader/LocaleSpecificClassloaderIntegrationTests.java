package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.sourceforge.jnlp.runtime.classloader2.JnlpApplicationClassLoader;
import net.sourceforge.jnlp.runtime.classloader2.Part;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;
import java.util.Locale;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFor;

@Execution(ExecutionMode.SAME_THREAD)
public class LocaleSpecificClassloaderIntegrationTests {

    private static Locale defaultLocale;

    @BeforeAll
    public static void init() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
    }

    @AfterAll
    public static void end() {
        Locale.setDefault(defaultLocale);
    }

    /**
     * Resources with a matching local will be loaded
     */
    @Test
    public void testLoadForConcreteLocale() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-12.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(2, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_2));
    }

    /**
     * Resources with a not matching local won't be loaded
     */
    @Test
    public void testNotLoadForWrongLocale() throws Exception {
        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final List<Part> parts = createFor("integration-app-13.jnlp").getParts();

        //when
        new JnlpApplicationClassLoader(parts, jarProvider);

        //than
        Assertions.assertEquals(1, jarProvider.getDownloaded().size());
        Assertions.assertTrue(jarProvider.hasTriedToDownload(JAR_1));
        Assertions.assertFalse(jarProvider.hasTriedToDownload(JAR_2));
    }


}
