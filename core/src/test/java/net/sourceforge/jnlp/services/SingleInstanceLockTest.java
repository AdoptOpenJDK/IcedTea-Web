package net.sourceforge.jnlp.services;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class SingleInstanceLockTest extends NoStdOutErrTest {

    @Test
    public void testCreateWithPort() throws Exception {
        URL codeBase = new URL("http://icedtea.classpath.org");
        final URL url = this.getClass().getClassLoader().getResource("net/sourceforge/jnlp/basic.jnlp");
        assertNotNull(url);
        final JNLPFile jnlpFile = new JNLPFile(url.openStream(), url, codeBase, new ParserSettings(false, false, false), null);
        assertNotNull(jnlpFile);

        final SingleInstanceLock sil = new SingleInstanceLock(jnlpFile);

        assertFalse(sil.exists());

        sil.createWithPort(123);
        assertTrue(sil.exists());
        assertEquals(123, sil.getPort());

        sil.createWithPort(456);
        assertTrue(sil.exists());
        assertEquals(456, sil.getPort());
    }
}
