package net.sourceforge.jnlp.tools;

import static org.junit.Assert.*;

import org.junit.Test;

public class JarCertVerifierTest {

    @Test
    public void testIsMetaInfFile() {
        final String METAINF ="META-INF";
        assertFalse(JarCertVerifier.isMetaInfFile("some_dir/" + METAINF + "/filename"));
        assertFalse(JarCertVerifier.isMetaInfFile(METAINF + "filename"));
        assertTrue(JarCertVerifier.isMetaInfFile(METAINF + "/filename"));
    }

}
