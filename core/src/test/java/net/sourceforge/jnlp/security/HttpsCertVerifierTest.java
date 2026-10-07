package net.sourceforge.jnlp.security;

import net.adoptopenjdk.icedteaweb.testing.tools.CodeSignerCreator;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpsCertVerifierTest {

    @Test
    public void mostSpecificCommonNameIsLastInDerOrder() throws Exception {
        // BC keeps the written order as DER order; the last CN is the most specific
        assertEquals("inner", HttpsCertVerifier.getMostSpecificCommonName(
                CodeSignerCreator.createCert("C=CZ, O=Org, CN=outer, CN=inner", new Date(), 1)));
        assertEquals("example.com", HttpsCertVerifier.getMostSpecificCommonName(
                CodeSignerCreator.createCert("CN=example.com, O=Org", new Date(), 1)));
        assertNull(HttpsCertVerifier.getMostSpecificCommonName(
                CodeSignerCreator.createCert("O=Org", new Date(), 1)));
    }
}
