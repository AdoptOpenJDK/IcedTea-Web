package net.sourceforge.jnlp.signing;

import net.sourceforge.jnlp.tools.CertInformation;
import org.junit.Test;

import java.io.File;

public class SignVerifyUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testFailOnNullResource() {
        new JarSigningHolder(SignVerifyUtils.getSignByMagic(null, p -> new CertInformation()), SignVerifyResult.SIGNED_NOT_OK);
    }

    @Test(expected = NullPointerException.class)
    public void testFailOnNullCertInformationProvider() {
        new JarSigningHolder(SignVerifyUtils.getSignByMagic(new File(""), null), SignVerifyResult.SIGNED_NOT_OK);
    }

}
