package net.sourceforge.jnlp.runtime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TranslatorTest {

    private Translator translator;

    @Before
    public void setup() throws IOException {
        translator = new Translator(createTestBundle());
    }

    @Test(expected = IllegalStateException.class)
    public void testTranslatorCreationWithMissingResourceBundle() {
        new Translator("unknown.Messages");
    }

    @Test
    public void testTranslateWithRegularKey() {
        String message = translator.translate("key");
        assertEquals("value", message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTranslateWithNullKey() {
        translator.translate(null);
    }

    @Test
    public void testTranslateNonExistingKey() {
        String message = translator.translate("unknown-key");
        assertEquals("no-resource", message);
    }



    @Test
    public void testTranslateMessageWithArgs() {
        String message = translator.translate("argkey", new Object[] {"Hello"});
        assertEquals("value Hello", message);
    }

    @Test
    public void testTranslateMessageWithApostrophe() {
        //Message format requires apostrophes to be escaped by using two ''
        //The properties files follow this requirement
        String message = translator.translate("apostrophekey");
        assertEquals("keywith'", message);
    }

    @Test
    public void singletonTest1() {
        String message = Translator.R("key");
        Assert.assertNotEquals("value", message);
    }
    @Test
    public void singletonTest2() {
        String message = Translator.R("unknown-key");
        Assert.assertTrue(message.contains("unknown-key"));
    }


    private ResourceBundle createTestBundle() throws IOException {
        File f = new File(System.getProperty("java.io.tmpdir"), "test.properties");
        f.createNewFile();
        f.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(f);
        String message = "key=value\n"
                + "argkey=value {0}\n"
                + "apostrophekey=keywith''\n"
                + Translator.MISSING_RESOURCE_PLACEHOLDER + "=no-resource\n";
        fos.write(message.getBytes());

        URL u = f.getParentFile().toURI().toURL();
        ClassLoader loader = new URLClassLoader(new URL[] {u});

        return ResourceBundle.getBundle("test", Locale.getDefault(), loader);
    }
}
