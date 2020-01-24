package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.classloader.Part;
import net.sourceforge.jnlp.JNLPFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;
import java.util.Locale;

import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_1;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.JAR_2;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createFile;
import static net.adoptopenjdk.icedteaweb.integration.classloader.ClassloaderTestUtils.createPartsFor;

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
    @RepeatedTest(10)
    public void testLoadForConcreteLocale() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-12.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(2, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_2));
    }

    /**
     * Resources with a not matching local won't be loaded
     */
    @RepeatedTest(10)
    public void testNotLoadForWrongLocale() throws Exception {
        //given
        final JNLPFile jnlpFile = createFile("integration-app-13.jnlp");
        final List<Part> parts = createPartsFor(jnlpFile);
        final DummyPartsHandler partsHandler = new DummyPartsHandler(parts, jnlpFile);

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assertions.assertEquals(1, partsHandler.getDownloaded().size());
        Assertions.assertTrue(partsHandler.hasTriedToDownload(JAR_1));
        Assertions.assertFalse(partsHandler.hasTriedToDownload(JAR_2));
    }


}
