package net.adoptopenjdk.icedteaweb.i18n;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_IO_TMPDIR;
import static org.junit.Assert.assertEquals;

public class TranslatorTest {

    private static final String KEY = "key";
    private static final String UNKNOWN_KEY = "unknown.key";
    private static final String ARGUMENT_KEY = "argument.key";
    private static final String APOSTROPHE_KEY = "apostrophe.key";

    private Translator translator;
    private Translator translatorWithBundleWithoutMissingResourceFallback;

    @Before
    public void setup() throws IOException {
        Locale.setDefault(Locale.ENGLISH);
        translator = new Translator(createTestBundleWithMissingResourceFallback());
        translatorWithBundleWithoutMissingResourceFallback = new Translator(createTestBundleWithoutMissingResourceFallback());
    }

    @Test(expected = IllegalStateException.class)
    public void testTranslatorCreationWithMissingResourceBundle() {
        new Translator("unknown.Messages");
    }

    @Test
    public void testTranslateWithRegularKey() {
        String message = translator.translate(KEY);
        assertEquals("value", message);
    }

    @Test
    public void testTranslateWithRegularKeyAndArguments() {
        String message = translator.translate(ARGUMENT_KEY, new Object[] {"argument1", "argument2"});
        assertEquals("value argument1 argument2", message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTranslateWithNullKey() {
        translator.translate(null);
    }

    @Test
    public void testTranslateNonExistingKey() {
        String message = translator.translate(UNKNOWN_KEY);
        assertEquals(UNKNOWN_KEY + "_en", message);
    }

    @Test(expected = IllegalStateException.class)
    public void testTranslateNonExistingKeyWithBundleWithoutMissingResourceFallback() {
        translatorWithBundleWithoutMissingResourceFallback.translate(UNKNOWN_KEY);
    }

    @Test
    public void testTranslateMessageWithApostrophe() {
        //Message format requires apostrophes to be escaped by using two ''
        //The properties files follow this requirement
        String message = translator.translate(APOSTROPHE_KEY);
        assertEquals("valuewith'", message);
    }

    @Test
    public void testExistenceOfMissingResourcePlaceholderInSingletonDefaultBundle() {
        String message = Translator.R(Translator.MISSING_RESOURCE_PLACEHOLDER);
        Assert.assertNotNull(message);
    }

    private ResourceBundle createTestBundleWithMissingResourceFallback() throws IOException {
        final File f = new File(System.getProperty(JAVA_IO_TMPDIR), "test.properties");
        f.createNewFile();
        f.deleteOnExit();

        final FileOutputStream fos = new FileOutputStream(f);
        final String message = "key=value\n"
                + "argument.key=value {0} {1}\n"
                + "apostrophe.key=valuewith''\n"
                + Translator.MISSING_RESOURCE_PLACEHOLDER + "={0}_{1}\n";
        fos.write(message.getBytes());

        final URL u = f.getParentFile().toURI().toURL();
        final ClassLoader loader = new URLClassLoader(new URL[] {u});

        return ResourceBundle.getBundle("test", Locale.getDefault(), loader);
    }

    private ResourceBundle createTestBundleWithoutMissingResourceFallback() throws IOException {
        final File f = new File(System.getProperty(JAVA_IO_TMPDIR), "test2.properties");
        f.createNewFile();
        f.deleteOnExit();

        final FileOutputStream fos = new FileOutputStream(f);
        final String message = "key=value\n";
        fos.write(message.getBytes());

        final URL u = f.getParentFile().toURI().toURL();
        final ClassLoader loader = new URLClassLoader(new URL[] {u});

        return ResourceBundle.getBundle("test2", Locale.getDefault(), loader);
    }
}
