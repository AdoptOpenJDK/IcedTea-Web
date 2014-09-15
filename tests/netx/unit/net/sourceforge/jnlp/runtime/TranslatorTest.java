package net.sourceforge.jnlp.runtime;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;

public class TranslatorTest {

    @Before
    public void setup() throws IOException {
        File f = new File(System.getProperty("java.io.tmpdir"), "test.properties");
        f.createNewFile();
        f.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(f);
        String message = "key=value\n"
                + "argkey=value {0}\n"
                + "RNoResource=no-resource\n";
        fos.write(message.getBytes());

        URL u = f.getParentFile().toURI().toURL();
        ClassLoader loader = new URLClassLoader(new URL[] {u});

        ResourceBundle bundle = ResourceBundle.getBundle("test", Locale.getDefault(), loader);
        Translator.getInstance().loadResourceBundle(bundle);
    }

    @Test
    public void testTranslateNonExistingMessage() {
        String message = Translator.R("doesn't-exist");
        assertEquals("no-resource", message);
    }

    @Test
    public void testTranslateNullMessage() {
        String message = Translator.R(null);
        assertEquals("no-resource", message);
    }

    @Test
    public void testTranslateMessage() {
        String message = Translator.R("key");
        assertEquals("value", message);
    }

    @Test
    public void testTranslateMessageWithArgs() {
        String message = Translator.R("argkey", new Object[] {"Hello"});
        assertEquals("value Hello", message);
    }
}
