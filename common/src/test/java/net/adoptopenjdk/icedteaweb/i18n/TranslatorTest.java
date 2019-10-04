package net.adoptopenjdk.icedteaweb.i18n;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

public class TranslatorTest {

    private static final String KEY = "key";
    private static final String UNKNOWN_KEY = "unknown.key";
    private static final String ARGUMENT_KEY = "argument.key";
    private static final String APOSTROPHE_KEY = "apostrophe.key";

    @Test(expected = IllegalStateException.class)
    public void testTranslatorCreationWithMissingResourceBundle() {
        new Translator("unknown.Messages", Locale.ENGLISH);
    }

    @Test
    public void testTranslateWithRegularKey() throws Exception {
        String message = createTestTranslator().translate(KEY);
        assertEquals("value", message);
    }

    @Test
    public void testTranslateWithRegularKeyAndArguments() throws Exception {
        String message = createTestTranslator().translate(ARGUMENT_KEY, "argument1", "argument2");
        assertEquals("value argument1 argument2", message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTranslateWithNullKey() throws Exception {
        createTestTranslator().translate(null);
    }

    @Test
    public void testTranslateNonExistingKey() throws Exception {
        String message = createTestTranslator().translate(UNKNOWN_KEY);
        assertEquals(UNKNOWN_KEY + "_en", message);
    }

    @Test
    public void testTranslateMessageWithApostrophe() throws Exception {
        //Message format requires apostrophes to be escaped by using two ''
        //The properties files follow this requirement
        String message = createTestTranslator().translate(APOSTROPHE_KEY);
        assertEquals("valuewith'", message);
    }

    @Test
    public void testExistenceOfMissingResourcePlaceholderInSingletonDefaultBundle() {
        String message = Translator.R(Translator.MISSING_RESOURCE_PLACEHOLDER);
        Assert.assertNotNull(message);
    }

    @Test(expected = IllegalStateException.class)
    public void testTranslateNonExistingKeyWithBundleWithoutMissingResourceFallback() throws Exception {
        new Translator(createTestBundleWithoutMissingResourceFallback(), Locale.ENGLISH).translate(UNKNOWN_KEY);
    }

    @Test
    public void testChainingOfResourceBundle() {
        final Translator translator = new Translator("net.adoptopenjdk.icedteaweb.i18n.res1", Locale.ENGLISH);
        translator.addBundleImpl(ResourceBundle.getBundle("net.adoptopenjdk.icedteaweb.i18n.res2", Locale.ENGLISH));

        assertEquals("11", translator.translate("foo"));
        assertEquals("22", translator.translate("bar"));
        assertEquals("22", translator.translate("baz"));
    }

    private Translator createTestTranslator() throws IOException {
        return new Translator(createTestBundleWithMissingResourceFallback(), Locale.ENGLISH);
    }

    private ResourceBundle createTestBundleWithMissingResourceFallback() throws IOException {
        final File f = new File(JavaSystemProperties.getJavaTempDir(), "test.properties");
        f.createNewFile();
        f.deleteOnExit();

        final FileOutputStream fos = new FileOutputStream(f);
        final String message = "key=value\n"
                + "argument.key=value {0} {1}\n"
                + "apostrophe.key=valuewith''\n"
                + Translator.MISSING_RESOURCE_PLACEHOLDER + "={0}_{1}\n";
        fos.write(message.getBytes());

        final URL u = f.getParentFile().toURI().toURL();
        final ClassLoader loader = new URLClassLoader(new URL[]{u});

        return ResourceBundle.getBundle("test", Locale.ENGLISH, loader);
    }

    private ResourceBundle createTestBundleWithoutMissingResourceFallback() throws IOException {
        final File f = new File(JavaSystemProperties.getJavaTempDir(), "test2.properties");
        f.createNewFile();
        f.deleteOnExit();

        final FileOutputStream fos = new FileOutputStream(f);
        final String message = "key=value\n";
        fos.write(message.getBytes());

        final URL u = f.getParentFile().toURI().toURL();
        final ClassLoader loader = new URLClassLoader(new URL[]{u});

        return ResourceBundle.getBundle("test2", Locale.ENGLISH, loader);
    }
}
