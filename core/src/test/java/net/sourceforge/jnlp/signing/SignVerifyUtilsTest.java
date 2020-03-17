package net.sourceforge.jnlp.signing;

import org.junit.Test;

public class SignVerifyUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testFailOnNullResource() {
        SignVerifyUtils.determineCertificatesFullySigningThe(null);
    }

}
